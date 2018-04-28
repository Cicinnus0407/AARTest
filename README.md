# AARTest
详解打包上传使用AAR


　　最近在做基础功能和架构搭建,所以会将基础功能进行封装,并提供一份远程依赖.在使用过程中踩了很多坑.特地进行记录<br>
  
　　不想看分析和流程的可以直接点击查看,重点关注工程的三个gradle文件和相关注释,MainActivity有引用库的类示例[示例代码](https://github.com/Cicinnus0407/AARTest)
  

 　　在Android中使用第三方的库和自己的库,无论是远程依赖还是本地依赖,通常会选择JAR和AAR的形式.而AAR相比JAR包可以包含资源文件.可以有AndroidManifest文件.所以都会建议使用AAR包的形式进行打包.<br>
　　网上很多示例都只是封装了自己的代码,而涉及到第三方的SDK引用,就避而不谈,或者是使用jar包的形式.但是使用jar包会有非常多的问题,例如第三方SDK包含了SO库(如高德地图,极光推送),而且由于是基础封装的库,所以必须包含所有的so库版本.就会导致aar包的非常庞大,这时就需要使用远程依赖和动态设置so支持版本来解决这个问题.而使用了远程依赖,又要解决依赖传递的问题.所以就有了两个主要解决的问题.<br>
　　默认的AAR包只有代码,没有注释和源码.
  
  
  
  
#### 需要解决的问题
- 1.aar包远程依赖第三方库
- 2.aar包依赖冲突
- 3.aar添加源码和代码注释

##### 解决问题前需要了解的两个知识点

![Android插件推荐配置](https://cicinnus-blog.oss-cn-shenzhen.aliyuncs.com/2018/04/Android%E6%8E%A8%E8%8D%90gradle%E4%BE%9D%E8%B5%96%E6%96%B9%E5%BC%8F.png)

从这张图中可以看到,从Android gradle plugin 3.0.0版本开始,推荐使用implementation和api替换到以前使用的compile依赖方法.

---
![gradle以来传递](https://cicinnus-blog.oss-cn-shenzhen.aliyuncs.com/2018/04/gradle%E4%BE%9D%E8%B5%96%E4%BC%A0%E9%80%92.png)
上面这张图是在gradle官网关于gradle处理依赖的截图.这里做个简单的整理

参数|值|意义
--|--|--
force|true|发生冲突时强制使用该依赖,false不强制使用,默认为false
exclude|module : 'lib'|去掉名为lib的module依赖
exclude|group:'org.xx'|去掉包名为org.xx依赖
transitive|false|不传递依赖,true为传递,默认为false

---
   
> 在使用过程中还是发现有一个问题依旧不能解决.如果对aar包进行了混淆,自己编写的代码都被去除了,不知道是什么原因,希望有高人相助.

---
   
   
#### 新建Module并打包AAR流程

##### 1.添加一个AndroidLibrary模块
```
右键工程父目录  -> New -> Module -> Android Library
```

##### 2.修改Library模块的build.gradle
```
//在最上方加上maven依赖的插件
...
apply plugin: 'maven'

...//这里忽略了gradle文件原有的配置

//在gradle文件最下方新建一个打包任务.
//maven上传,示例代码将aar输出到了本地,如果公司有maven私服,可以将地址改为maven私服的地址
def MAVEN_LOCAL_PATH = 'file://localhost/' + rootProject.buildDir
//版本号
def VERSION = '0.0.1'
//公司名称
def GROUP_ID = 'com.cicinnus.aartest'
//aar包名称
def ARTIFACT_ID = 'aartest'
//Maven私服账号
//def ACCOUNT = 'xxx'
//Maven私服密码
//def PWD = 'xxx'
uploadArchives {
    repositories {
        mavenDeployer {
            repository(url: MAVEN_LOCAL_PATH) {
                //如果使用的是maven私服,一般都需要账号密码
				//authentication(userName :ACCOUNT,password:PWD)
            }
            pom.groupId = GROUP_ID
            pom.artifactId = ARTIFACT_ID
            pom.version = VERSION
            pom.packaging = 'aar'

        }
    }
}
```

##### 3.构建aar,输出到本地
```
在项目目录,执行命令行./gradlew uploadArchives
获取在AS右边的gradle插件中
选择library模块 -> upload -> 双击uploadArchives .等待编译结束
编译结束后在Library模块 -> outputs -> aar .就可以找到编译好的aar包
```

##### 4.引用本地aar(因为缺失对maven的依赖管理,所以本地引用会导致依赖无法正确传递,即使用本地aar包的方式会导致远程依赖失效.解决方案请继续往下)
```
在app模块的gradle文件添加
...
repositories {
    flatDir {
        dirs 'libs'
    }
}
dependencies {
	...
    api(name: 'aarlibrary-release', ext: 'aar')
}
```

##### 5.使用远程依赖aar,推荐的解决方案

使用远程依赖的方式:
```
1.maven构建的地址填写maven私服地址
def MAVEN_LOCAL_PATH = 'maven私服地址'
uploadArchives {
    repositories {
        mavenDeployer {
            repository(url: MAVEN_LOCAL_PATH) {
            }   
       ...省略部分代码
     }
  }
}   

2.在工程父目录的gradle文件添加maven仓库地址

allprojects {
    repositories {
        maven {
            url 'maven私服地址,与上传的地址一致'
        }
        google()
        jcenter()
    }

}
3.app模块的gradle使用远程依赖,并传递依赖,注意最后的transitive
    api group: 'com.cicinnus.aartest', name: 'aartest', version: '0.0.1', transitive: true


```

#### 总结最佳方案
使用远程aar,最大优势:
- .方便处理依赖冲突,依赖传递

不建议使用本地aar的两个原因:
- 1.aar中的第三个库无法使用远程依赖
- 2.如果使用本地maven,需要每个参与开发的人员都配置一个本地的maven仓库,不现实

#### aar最佳使用流程和推荐用法

- 1.配置gradle task,上传aar包到maven仓库
- 2.配置远程依赖,将依赖的版本号设为'+'.意为动态使用最新版本,可以解决aar更新后无法及时知道的问题
> implementation group: 'com.cicinnus.aartest', name: 'aartest', version: '+',transitive:true
- 3.有A,B,C三个模块,A为app主模块,A依赖B和C,B依赖C的部分功能.这时候可以考虑将B的依赖关键字使用compileOnly,就可以解决最终的依赖冲突问题.却又能在编译期正常使用C模块的功能.
> compileOnly  group: 'com.cicinnus.aartest', name: 'aartest', version: '0.0.1'


