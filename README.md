# easyHttpProxy
[![author](https://img.shields.io/badge/author-puhaiyang-brightgreen)](https://github.com/puhaiyang)
[![blog](https://img.shields.io/badge/blog-csdn-brightgreen)](https://blog.csdn.net/puhaiyang)
[![License](https://img.shields.io/github/license/puhaiyang/easyHttpProxy)](https://github.com/puhaiyang/easyHttpProxy/blob/master/LICENSE)

support http/https proxy.类似于fiddler,由java编写，代码简单便于理解。支持http/https代理！

## 环境
[![jdk](https://img.shields.io/badge/jdk-1.8%2B-brightgreen)](https://github.com/puhaiyang)
[![jdk](https://img.shields.io/badge/netty-4.1%2B-brightgreen)](https://github.com/puhaiyang)
[![jdk](https://img.shields.io/badge/maven-3.0%2B-brightgreen)](https://github.com/puhaiyang)
- jdk 1.8+
- maven 3.0+
- netty 4.1+

## 使用方法
添加依赖
```
<dependency>
  <groupId>com.github.puhaiyang</groupId>
  <artifactId>easy-http-proxy</artifactId>
  <version>0.0.1</version>
</dependency>
```
启动代理服务器：
> EasyHttpProxyServer.getInstace().start(6667);

## 生成ca根证书
- 下载openssl工具
> [https://slproweb.com/products/Win32OpenSSL.html][https://slproweb.com/products/Win32OpenSSL.html]，在windows下默认会在C:\Program Files\Open SSL Win64目录下，需要将其配到path

说明
```
    openssl genrsa
功能：
    用于生成RSA私钥，不会生成公钥，因为公钥提取自私钥
使用参数：
    openssl genrsa [-out filename] [-passout arg] [-des] [-des3] [-idea] [numbits]
选项说明：
    -out filename     ：将生成的私钥保存至filename文件，若未指定输出文件，则为标准输出。
    -numbits            ：指定要生成的私钥的长度，默认为1024。该项必须为命令行的最后一项参数。
    -des|-des3|-idea：指定加密私钥文件用的算法，这样每次使用私钥文件都将输入密码，太麻烦所以很少使用。
    -passout args    ：加密私钥文件时，传递密码的格式，如果要加密私钥文件时单未指定该项，则提示输入密码。传递密码的args的格式见一下格式。
    a.pass:password   ：password表示传递的明文密码
    b.env:var               ：从环境变量var获取密码值
    c.file:filename        ：filename文件中的第一行为要传递的密码。若filename同时传递给"-			
    passin"和"-passout"选项，则filename的第一行为"-passin"的值，第			
    二行为"-passout"的值
    d.stdin                   ：从标准输入中获取要传递的密码
```
- 生成rsa私钥,输出ca.key文件
> openssl genrsa -des3 -out ca.key 2048
- 生成ca根证书,以ca.key作为私钥,输出ca.crt证书
> openssl req -sha256 -new -x509 -days 36500 -key ca.key -out ca.crt -subj "/C=CN/ST=GD/L=SZ/O=haiyang/OU=study/CN=HaiyangProxy"
- 将ca.key私钥转为der格式的私钥,输出ca_private.der文件
> openssl pkcs8 -topk8 -nocrypt -inform PEM -outform DER -in ca.key -out ca_private.der

[https://slproweb.com/products/Win32OpenSSL.html]: https://slproweb.com/products/Win32OpenSSL.html