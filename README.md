# (README IS STILL UNDER CONSTRUCTION)

This is the next generation of <a href="https://github.com/bukalapak/url-router">URL Router</a>

# Neuro
A wrapper for easily routing URL on Android

## Biological Anatomy

![Neuro Anatomy](https://github.com/bukalapak/neuro/blob/master/images/neuron_anatomy.jpg)

## Simple Routing 

You need this if you have only 1 domain to be processed.

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

```kotlin
NeuroNetwork.proceed(url)
```

## Advanced Routing

You need this if your routing is super complex

// blablabla

### Language

#### Wildcard : `*`
- Can be placed anywhere except query
- Used for replacing any character inside URL
- Regex: `.+` -> Any character more than 1

#### Variable Name : `<variable_name>`
- Can be placed anywhere except query
- Used for getting value inside URL
- Variable name can only use `A-Z`, `a-z`, `0-9`, and `_`
- Default regex: `[^/]+` -> Any character more than 1 except `/`
- Default regex can be changed

#### Variable Name With Specific Pattern : `<variable_name:[a-z0-9]+>`
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
implementation '...'
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
