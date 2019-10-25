package com.github.puhiayang.utils;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * https支持工具类
 *
 * @author puhaiyang
 * created on 2019/10/25 22:27
 */
public class HttpsSupport {
    /**
     * 证书
     */
    private SslContext clientSslCtx;
    /**
     * 证书使用者
     */
    private String issuer;
    /**
     * 证书开始时间
     */
    private Date caNotBefore;
    /**
     * 证书结束时间
     */
    private Date caNotAfter;
    /**
     * ca私钥
     */
    private PrivateKey caPriKey;
    /**
     * 服务端私钥
     */
    private PrivateKey serverPriKey;
    /**
     * 服务端公钥
     */
    private PublicKey serverPubKey;

    /**
     * 证书cahce
     */
    private Map<String, X509Certificate> certCache = new HashMap<>();
    /**
     *
     */
    private KeyFactory keyFactory = null;

    private HttpsSupport() {
        initHttpsConfig();
    }

    private static HttpsSupport httpsSupport;

    public static HttpsSupport getInstance() {
        if (httpsSupport == null) {
            httpsSupport = new HttpsSupport();
        }
        return httpsSupport;
    }

    private void initHttpsConfig() {
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            //信任客户端的所有证书,不进行校验
            setClientSslCtx(SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build());
            //加载证书
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            //从项目目录加入ca根证书
            X509Certificate caCert = loadCert(classLoader.getResourceAsStream("ca.crt"));
            //从项目目录加入ca私钥
            PrivateKey caPriKey = loadPriKey(classLoader.getResourceAsStream("ca_private.der"));
            setCaPriKey(caPriKey);
            //从证书中获取使用者信息
            setIssuer(getSubjectByCert(caCert));
            //设置ca证书有效期
            setCaNotBefore(caCert.getNotBefore());
            setCaNotAfter(caCert.getNotAfter());
            //生产一对随机公私钥用于网站SSL证书动态创建
            KeyPair keyPair = genKeyPair();
            //server端私钥
            setServerPriKey(keyPair.getPrivate());
            //server端公钥
            setServerPubKey(keyPair.getPublic());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成RSA公私密钥对,长度为2048
     */
    private KeyPair genKeyPair() throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeyPairGenerator caKeyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
        caKeyPairGen.initialize(2048, new SecureRandom());
        return caKeyPairGen.genKeyPair();
    }

    /**
     * 获取证书中的subject信息
     */
    private String getSubjectByCert(X509Certificate certificate) {
        //读出来顺序是反的需要反转下
        List<String> tempList = Arrays.asList(certificate.getIssuerDN().toString().split(", "));
        return IntStream.rangeClosed(0, tempList.size() - 1)
                .mapToObj(i -> tempList.get(tempList.size() - i - 1)).collect(Collectors.joining(", "));
    }

    /**
     * 加载ca的私钥
     *
     * @param inputStream ca私钥文件流
     */
    private PrivateKey loadPriKey(InputStream inputStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bts = new byte[1024];
        int len;
        while ((len = inputStream.read(bts)) != -1) {
            outputStream.write(bts, 0, len);
        }
        inputStream.close();
        outputStream.close();
        return loadPriKey(outputStream.toByteArray());
    }

    /**
     * 从文件加载RSA私钥 openssl pkcs8 -topk8 -nocrypt -inform PEM -outform DER -in ca.key -out
     * ca_private.der
     */
    private PrivateKey loadPriKey(byte[] bts)
            throws Exception {
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bts);
        return keyFactory.generatePrivate(privateKeySpec);
    }

    /**
     * 加载ca根证书
     *
     * @param inputStream 证书文件流
     */
    private X509Certificate loadCert(InputStream inputStream) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(inputStream);
    }

    public SslContext getClientSslCtx() {
        return clientSslCtx;
    }

    public void setClientSslCtx(SslContext clientSslCtx) {
        this.clientSslCtx = clientSslCtx;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Date getCaNotBefore() {
        return caNotBefore;
    }

    public void setCaNotBefore(Date caNotBefore) {
        this.caNotBefore = caNotBefore;
    }

    public Date getCaNotAfter() {
        return caNotAfter;
    }

    public void setCaNotAfter(Date caNotAfter) {
        this.caNotAfter = caNotAfter;
    }

    public PrivateKey getCaPriKey() {
        return caPriKey;
    }

    public void setCaPriKey(PrivateKey caPriKey) {
        this.caPriKey = caPriKey;
    }

    public PrivateKey getServerPriKey() {
        return serverPriKey;
    }

    public void setServerPriKey(PrivateKey serverPriKey) {
        this.serverPriKey = serverPriKey;
    }

    public PublicKey getServerPubKey() {
        return serverPubKey;
    }

    public void setServerPubKey(PublicKey serverPubKey) {
        this.serverPubKey = serverPubKey;
    }


    /**
     * 获取证书
     *
     * @param host host
     * @return host对应的证书
     */
    public X509Certificate getCert(String host) throws Exception {
        if (StringUtils.isBlank(host)) {
            return null;
        }
        X509Certificate cacheCert = certCache.get(host);
        if (cacheCert != null) {
            //将缓存的证书返回
            return cacheCert;
        }
        //生成新的证书，并将它放到缓存中去
        host = host.trim().toLowerCase();
        String hostLowerCase = host.trim().toLowerCase();
        X509Certificate cert = genCert(getIssuer(), getCaPriKey(), getCaNotBefore(), getCaNotAfter(), getServerPubKey(), hostLowerCase);
        //添加到缓存
        certCache.put(host, cert);
        return certCache.get(host);
    }

    /**
     * 动态生成服务器证书,并进行CA签授
     *
     * @param issuer 颁发机构
     */
    /**
     * @param issuer        颁发机构
     * @param caPriKey      ca私钥
     * @param certStartTime 证书开始时间
     * @param certEndTime   证书结束时间
     * @param serverPubKey  server证书的公钥
     * @param hosts         host，支持同时生成多个host
     * @return 证书
     * @throws Exception Exception
     */
    public static X509Certificate genCert(String issuer, PrivateKey caPriKey, Date certStartTime,
                                          Date certEndTime, PublicKey serverPubKey,
                                          String... hosts) throws Exception {
        //根据CA证书subject来动态生成目标服务器证书的issuer和subject
        String subject = "C=CN, ST=SC, L=CD, O=hai, OU=study, CN=" + hosts[0];
        JcaX509v3CertificateBuilder jv3Builder = new JcaX509v3CertificateBuilder(new X500Name(issuer),
                //序列号，需要唯一;ElementaryOS上证书不安全问题(serialNumber为1时证书会提示不安全)，避免serialNumber冲突，采用时间戳+4位随机数生成
                BigInteger.valueOf(System.currentTimeMillis() + (long) (Math.random() * 10000) + 1000),
                certStartTime,
                certEndTime,
                new X500Name(subject),
                serverPubKey);
        //SAN扩展证书支持的域名，否则浏览器提示证书不安全
        GeneralName[] generalNames = new GeneralName[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            generalNames[i] = new GeneralName(GeneralName.dNSName, hosts[i]);
        }
        GeneralNames subjectAltName = new GeneralNames(generalNames);
        //添加多域名支持
        jv3Builder.addExtension(Extension.subjectAlternativeName, false, subjectAltName);
        //SHA256 用SHA1浏览器可能会提示证书不安全
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(caPriKey);
        return new JcaX509CertificateConverter().getCertificate(jv3Builder.build(signer));
    }
}
