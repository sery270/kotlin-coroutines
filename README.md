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
    - swift, javascript, etc

### CoroutineScope

- 모든 코루틴은 **CoroutineScope 안에서 실행된다.**
- CoroutineScope내에선 sleep 대신 suspend 함수인 delay을 사용한다.
- 해당 Job이 취소되면, 해당 Scope 및 Job의 모든 코루틴이 취소된다.

### ViewModelScope

- ViewModelScope은 사전에 정의된 CoroutineScope이다. 
- onCleared 호출 시 자동으로 취소된다.
- ViewModelScope는 기본적으로`Dispatchers.Main.immediate` 를 사용한다.

### With Retrofit

- 내부적으로, Retrofit은 새 Call 개체를 만들고 요청을 비동기식으로 보내기 위해 enqueue를 호출한다.
- Suspend function support requires Retrofit 2.6.0 or higher.
- Retrofit은 해당 요청함수를 **main-safety**하게 만든다.
  - suspend 함수 내에서 Retrofit은 백그라운드 스레드에서 네트워크 요청을 실행하고, 호출이 완료되면 코루틴을 재개한다. 
  - 따라서 **main-safe하다.**
- Retrofit은 사용자 지정 디스패처를 사용하며 Dispatchers.IO를 사용하지 않는다.

### With Room

- Room은 쿼리를 **main-safety**하게 만들고, 자동으로 백그라운드 스레드에서 쿼리를 실행한다.
- Room은 구성된 기본 쿼리 및 [transaction](https://developer.android.com/reference/androidx/room/RoomDatabase.Builder.html?hl=ko#setTransactionExecutor(java.util.concurrent.Executor)) `Executor`를 사용하여 코루틴을 실행한다.
- Room은 사용자 지정 디스패처를 사용하며 Dispatchers.IO를 사용하지 않는다.

### Coroutines Exceptions

- Callback의 단점 중 하나 였던 부분을 해결한다. (일부 언어 기능을 지원하지 못한다. Exception 처리)
    - rely on the built-in language support for error handling instead of building custom error handling for every callback.
- 코루틴에서 에러가 발생하면, caller에게 오류 던지기 때문에, 일반 함수처럼 try/catch로 예외처리를 할 수 있다.
- catch로 못잡은 에러들은 JVM에서 스레드의 포착되지 않은 예외 처리기로 전송된다.
    - CoroutineExceptionHandler를 제공하여 이 동작을 사용자 정의할 수 있다.
- 만약에 코루틴에서 에러가 발생하면, 코루틴은 기본적으로 부모 코루틴을 취소한다. 다른 자식들에게 전파되어 취소된다. 
    ![Untitled](https://user-images.githubusercontent.com/59532818/164750285-4bd6ddfa-685c-43f2-97d7-3d5276b5f341.png)



### suspend lambda (****higher order functions)****

- 람다를 적절히 사용하면, 중복 코드 제거하고 캡슐화하는데에 도움을 줄 수 있다.
    
    ```kotlin
    // MainViewModel.kt
    
    private fun launchDataLoad(block: suspend () -> Unit): Job {
       return viewModelScope.launch {
           try {
               _spinner.value = true
               block()
           } catch (error: TitleRefreshError) {
               _snackBar.value = error.message
           } finally {
               _spinner.value = false
           }
       }
    }
    
    fun refreshTitle() {
       launchDataLoad {
           repository.refreshTitle()
       }
    }
    ```
    
- suspend 함수도 람수로 표현할 수 있다. 
    - To make a suspend lambda, start with the `suspend` keyword.
        
        ```kotlin
        // suspend lambda
        
        block: suspend () -> Unit
        ```
        

### **WorkManager**

- WorkManager는 지연 가능한 백그라운드 작업을 위한 호환 가능하고 유연하며 간단한 라이브러리이다.
- WorkManager는 [Android Jetpack](http://d.android.com/jetpack)이고, AAC이며, **Opportunistic execution**와 **Guaranteed execution**이 필요한 백그라운드 작업을 위한 것이다.
    - **Opportunistic execution** → WorkManager가 가능한 한 빨리 백그라운드 작업을 수행
    - **Guaranteed execution** → 앱에서 벗어나 탐색하더라도 다양한 상황에서 작업을 시작할 수 있는 로직을 처리
- WorkManager가 하면 좋은 작업들
    - Uploading logs
    - Applying filters to images and saving the image
    - Periodically syncing local data with the network

### **CoroutineWorker**

- `CoroutineWorker.doWork()` is a suspending function
- `CoroutineWorker`는 기본적으로 `Dispatchers.Default` 를 사용한다. 
- `CoroutineWorker`는 `ListenableWorker`을 상속 받는다.
- WorkManager v2.1 부터 `ListenableWorker`을 더 쉽게 테스트할 수 있는 API를 제공한다. → [TestListenableWorkerBuilder](https://developer.android.com/reference/androidx/work/testing/TestListenableWorkerBuilder)

### Tips

- you might declare one(Scopes) in a RecyclerView Adapter to do DiffUtil operations.

### 🚧 Testing Coroutine 🚧 

- 추후 보강 예정,,, 어렵다...
- [https://tourspace.tistory.com/266](https://tourspace.tistory.com/266)
- `MainCoroutineScopeRule`
- `InstantTaskExecutorRule`
  
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
