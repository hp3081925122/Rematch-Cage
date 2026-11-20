# 26.1.2 NeoForge 模板

这是一个无 Mixin、无自定义内容的最小 NeoForge 工作区，使用 NeoForge 26.1.2.116 与 Java 25。

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\jdk-25.0.4+7'
.\gradlew.bat build
.\gradlew.bat runClient
```

复制项目和批量改名请在上一级目录运行 `new-mod-from-template.ps1`。
