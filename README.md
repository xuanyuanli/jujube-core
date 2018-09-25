# 初衷
每个项目都会有自己的核心工具二方包，这个二方包一般满足以下特点：
- 对项目常用的各种工具操作进行封装  
- 随着项目经验的累加，不断完善自己的工具包  

特别是第二点，就像一个熟练的汽车工程师会去积累自己的工具和经验一样，程序员在开发过程中也是不断积累自己的工具包的。  
一般来说，其他工具包会着重一个特点：轻量级，不会依赖太多其他三方包。  
但jujube-core的理念不是这样的，他也集中了不少业界流行的三方包作为基础，但他更好的用途还是作为一个二方包存在比较好。  
你如果需要一个三方包，还有很多其他选择，如Hutool等。  

# 依赖的其他三方包
依次如下：
- 图像处理。处理图片有时出现读取错误，用[metadata-extractor](https://drewnoakes.com/code/exif/)来解决，处理缩略图用[thumbnailator](http://code.google.com/p/thumbnailator)来处理
- 二维码生成与扫码。用到com.google.zxing
- office文件处理。csv用common-csv处理，Word、Excel用poi处理
- Json处理。用SpringFramework默认的Jackson处理
- 汉字拼音。用到pinyin4j
- 日志。推荐用logback，如果用log4j，请去掉logback-classic和log4j-over-slf4j的依赖
- 流行的工具包。包括common全家桶和guava
- 字节码处理。用到javassist和cglib
- http处理。用到unirest
- 模板处理。用到Freemarker
- Spring相关。Spring的包全是optional的包，用到的时候再引入
- servlet-api和lombok。一个是JavaEE Servlet标准，一个是动态字节码生成，他们全是provided

# 自定义的工具
主要分为三大类，分别是常量类、基础类和工具类。

使用javadoc -d命令生成javadoc，可以看到所有类的描述。


