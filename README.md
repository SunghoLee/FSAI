# FSAI
A Fast Scanner for Activity Injection(FSAI) is a static analysis tool that detects Activity injection cases in Android apps. 

## Getting Started

### Prerequisites

* JDK 1.8.0
* Gradle 4.3.1 (for build)

### Building FSAI

Simply, execute a build script as follows:

```
bash-3.2$ ./build.sh
```

When it succeeds in building FSAI, build success message would be printed as follows:

```
BUILD SUCCESSFUL in [xx]s
```

After builidng FSAI, the script makes FSAI.jar in the same directory in which it is.

## Running FSAI

At first, Java Runtime directory and Android rt file must be set in wala.properties file as follows:

```
// Java runtime directory
java_runtime_dir = /Library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/Contents/Home/jre/lib
// Android rt directory
android_rt_jar = /Users/leesh/Documents/repo/HybriDroid/data/android.jar
```

Using the paths of the wala.properties file and a analysis target app as arguments, you could run FSAI as follows:

```
java -jar FSAI.jar wala.properties [targetAppPath]
```

## License

This project is licensed under the Eclipse Public License v1.0
