## 20220422

## Coroutines in Kotlin

### Main-Safety

- 메인 스레드는 **막힘없는 UI 작업**을 위해 **Single Thread** 모델을 차용하고 있다. (UI 업데이트, 클릭 이벤트, UI Callback)
- [막힘 없는 UI 작업을 위해, **메인 스레드는** **약 16ms 마다 화면을 업데이트** 해야한다.]([https://medium.com/androiddevelopers/exceed-the-android-speed-limit-b73a0692abc1](https://medium.com/androiddevelopers/exceed-the-android-speed-limit-b73a0692abc1))
- 하지만 여러 작업들은 16ms 보다 긴 처리 시간을 요구한다. (네트워크 통신, 데이터베이스 작업, JSON 파싱 등등)
- 따라서 메인 스레드에서의 긴 작업들은 막힘없는 UI 작업에 영향을 줄 수 있다. 즉, 화면이 버벅거리거나 멈추거나 혹은 ANR과 같은 상황을 발생시킬 수 있다.
- 따라서 안드로이드 개발에 있어, 메인 스레드를 blocking하지 않는 것은 매우 중요하다.
- **Main-Safety**하다는 것은, 위에서 설명한 메인스레드의 역할이 잘 동작하도록, 메인 스레드를 blocking 하지 않는 것을 의미한다.

### Callback vs Coroutine

- **Main-Safety**하게 긴 작업을 처리하는 방법 중 하나로 **Callback**이 있다.
    - Callback은 백그라운드 스레드를 사용하여 작업을 실행하고, 백그라운드 스레드에서 작업이 완료되면 메인 스레드의 Callback을 호출하여 작업 결과를 알려주는 방법이다.
    - 메인 스레드가 백그라운드 스레드에게 작업을 요청한 후엔, 당연히 메인 스레드는 다른 작업을 수행할 수 있다. 
      - 작업이 완료되면 Callback을 통해 작업 결과를 공유 받을 것이기에, 메인 스레드가 백그라운드 스레드의 작업 완료 여부를 계속 신경쓰고 있지 않기 때문이다. 
    - 따라서 Callback은 Main-Safety하다.
- Callback은 Main-Safety한 작업을 하게 해주지만, 아래와 같은 단점이 있다.
    - 가독성이 좋지 않다.
    - 무슨 내용인지 추론하기 어렵다.
    - 일부 언어 기능을 지원하지 못한다. (Exception 처리)
- Callback과 Coroutine은 Main-Safety한 작업을 하게 해준다는 점에서 같은 일을 하지만, Coroutine을 사용한다면 위에서 언급한 Callback의 단점을 없앨 수 있다.
    
    

### Coroutines by another name

- `async` 와 `await`으로 사용되는 패턴들은 Coroutine을 베이스로 한다.
    - swift, javascript, etc ,,,

---

## dependencies

```groovy
dependencies {
  implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:x.x.x"
  implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:x.x.x"
  implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:x.x.x"
}
```

- **kotlinx-coroutines-core**
    - Main interface for using coroutines in Kotlin
- **kotlinx-coroutines-android**
    - Support for the Android Main thread in coroutines
- **lifecycle-viewmodel-ktx**
    - Adds a CoroutineScope to ViewModels that's configured to start UI-related coroutines

## Project Struct

![https://developer.android.com/codelabs/kotlin-coroutines/img/cbc7d16909facb7c.png?hl=ko](https://developer.android.com/codelabs/kotlin-coroutines/img/cbc7d16909facb7c.png?hl=ko)

- `MainActivity`
    - displays the UI
    - registers click listeners
    - display a `Snackbar`
    - passes events to `MainViewModel` and updates the screen based on `LiveData` in `MainViewModel`
- `MainViewModel`
    - handles events in `onMainViewClicked`
    - communicate to `MainActivity` using `LiveData`
- `Executors`
    - defines `BACKGROUND`
        - which can run things on a background thread.
- `TitleRepository`
    - fetches results from the network
    - saves them to the database
---
# Using Kotlin Coroutines in your Android app

This folder contains the source code for the [Kotlin Coroutines codelab](https://codelabs.developers.google.com/codelabs/kotlin-coroutines/index.html).

## License

    Copyright 2018 Google LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
