****一款可以自己按需定制的代码生成工具****

适用场景：springmvc mybatis mysql

使用说明：
1.找到generate.xml文件，修改相应标签元素：如<entry key="generateTables">t_incallring</entry>

    <!-- 需要根据实际情况改动的部分  开始-->
    <!-- commonspackage 包命名路径，开发中需要根据具体情况需要此路径，但开头必须是com.xxx,存放公用的java文件 -->
    <entry key="commonspackage">com.xxx.yyy</entry>

2.找到GenerateMain.java文件，执行main方法即可。

3.根据业务定制自己的模板，支持前端生成jsp（被我删了）。

4.将生成的java文件放到自己的项目中，**依赖的基类和分页model在common目录下**，如果有自己的抽象类或分页方法，定制即可。

## 补充

这些被我删掉了

```
<#include "/java_copyright.include"> 
<#include "/macro.include">
```

controller里的一些方法也被我删掉了，因为我觉得太多也不好，模板简单点就行了。还有同意返回形式也可以自己设置

mapper.xml的一些路径要改

```
<mapper namespace="com.uxin.${modulename}.dao.${className}Mapper" >
  <resultMap id="BaseResultMap" type="com.uxin.${modulename}.domain.${className}" >
```

dao路径下可自定义加上mapper

domain也可自定义换成entity

启动的类要加上mapperscan注解

https://blog.csdn.net/qq_39597203/article/details/85069880

帮助我mvn 成功了
