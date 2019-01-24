# (README IS STILL UNDER CONSTRUCTION)

This is the next generation of <a href="https://github.com/bukalapak/url-router">URL Router</a> with cleaner code and imrpoved performce up to 3000% faster that previous version, support modularization, easier to use, and many other benefits!

## Biological Anatomy

![Neuro Anatomy](https://github.com/bukalapak/neuro/blob/master/images/neuron_anatomy.jpg)

## Simple Routing 

You need this if you have only 1 domain to be processed:

```kotlin
SimpleNeuro.setBase(Uri.parse("https://www.mywebsite.com"))

// https://www.mywebsite.com/login
SimpleNeuro.addPath("/login") {
   // goto login
}

// https://www.mywebsite.com/messages/1234
SimpleNeuro.addPath("/messages/<message_id>") {
   val messageId = it.variables.optString("message_id")
   // open message with id 1234
}

// https://www.mywebsite.com/promo?source=banner
SimpleNeuro.addPath("/promo") {
   val source = it.queries.optString("source")
   // open promo with source `banner`
}
```

And use this to execute URL:

```kotlin
SimpleNeuro.proceed(url)
```

## Advanced Routing

You need this if your routing is super complex:

### Define Somas

Soma is used to filter base URL including scheme, domain, and port.

There are 3 types of Soma class:

1. `Soma`: URL that has one or more paths to be processed, multiple instances are allowed.

```kotlin
object MyWebsiteSoma : Soma("my_website") {
   
   val schemes = listOf("https")
   val hosts = listOf("www.mywebsite.com", "m.mywebsite.com")
   
   override fun onSomaProcess(signal: Signal) {
      // preprocess URL here
      // return false/super if you need to forward process to branch
      // return true if you want to stop processing
      return super.onSomaProcess(signal)
   }
   
   override fun onProcessNoBranch(signal: Signal) {
      // it will be passed if URL inputted has no path, like: https://www.website.com
   }
   
   override fun onProcessOtherBranch(signal: Signal) {
      // it will be passed if where is no match for URL in listed branch, like: https://www.website.com/unlistedpath
   }
   
}
```

2. `SomaOnly`: URL that doesn't have path to be processed, multiple instances are allowed.

```kotlin
object MyOtherWebsiteSoma : SomaOnly("my_other_website) {
   
   val schemes = listOf("http")
   val hosts = listOf("www.myotherwebsite.com")
   val ports = listOf(8089)
   
   override fun onSomaProcess(signal: Signal) {
      // process URL
   }
  
}
```

3. `SomaFallback`: This is a fallback when there is no match for other defined Somas, only single instance is allowed.

```kotlin
object OthersSoma : SomaFallback() {
  
   override fun onSomaProcess(signal: Signal) {
      // process URL
   }
}
```

Notes:
- It's a good practise to make Soma subclasses in singleton object, because it's easier to manage and reusable
- If you don't define `schemes`, `hosts`, and `ports`, it means that it's optional, any value or no value will be accepted.

### Connect AxonBranches to Soma

AxonBranch is used to filter path after being processed by Soma.

Register all paths in your `MainActivity`, or even better in subclass of `Application`.

```kotlin
override fun onCreate() {
   Neuro.connect(MyWebsiteSoma, AxonBranch("/login") {
      // goto login
   })
   
   Neuro.connect(MyWebsiteSoma, AxonBranch("/messages/<message_id>") {
      val messageId = it.variables.optString("message_id")
      // open message with id 1234
   }

   Neuro.connect(MyWebsiteSoma, AxonBranch("/promo") {
      val source = it.queries.optString("source")
      // open promo with source `banner`
   }
   
   // for SomaOnly and SomaFallback
   
   Neuro.connect(MyOtherWebsiteSoma)
   
   Neuro.connect(OtherSoma)

}
```

## Other Advanced Features

1. Separate process between finding and processing route for optimization.
2. Pass context from `proceed` into `SignalAction` callback.
3. Pass any params bundled in `args` from `procees` into `SignalAction` callback.
4. Intercept `SignalAction` before doing process on each `proceed` call.
5. Intercept globally `SignalAction` before doing any process to it.
6. Find route without processing the URL.
7. Set priority for sorting in `Soma` and `SomaOnly`.
8. Expression for `scheme`, `host`, and `path` is highly configurable.
9. May use `getString()` for nullability or `optString()` for nonnullability.

## Expression language

Used to define `scheme`, `host`, and `path`.

### Wildcard : `*`
- Can be placed anywhere except query
- Used for replacing any character inside URL
- Regex: `.+` -> Any character more than 1

### Variable Name : `<variable_name>`
- Can be placed anywhere except query
- Used for getting value inside URL
- Variable name can only use `A-Z`, `a-z`, `0-9`, and `_`
- Default regex: `[^/]+` -> Any character more than 1 except `/`
- Default regex can be changed

### Variable Name With Specific Regex : `<variable_name:[a-z0-9]+>`
- Can be placed anywhere except query
- Used for getting value inside URL
- Variable name can only use `A-Z`, `a-z`, `0-9`, and `_`
- Default regex: `[^/]+` -> Any character more than 1 except `/`
- Default regex can be changed
- Variable name and regex separated with `:`
- Specific regex overrides default regex

## Installation

### Gradle

Add this line in your `build.gradle` file:

```
implementation '...neuro:2.0.0'
```

This library is hosted in the [JCenter repository](https://bintray.com/mrhabibi/maven), so you have to ensure that the repository is included:

```
buildscript {
   repositories {
      jcenter()
   }
}
```

## Contributions

Feel free to create issues and pull requests.

## License

```
URL Router library for Android
Copyright (c) 2018 Bukalapak (http://github.com/bukalapak).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
