/*
 * Copyright (C) 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.kotlincoroutines.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.kotlincoroutines.util.singleArgViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * MainViewModel designed to store and manage UI-related data in a lifecycle conscious way. This
 * allows data to survive configuration changes such as screen rotations. In addition, background
 * work such as fetching network results can continue through configuration changes and deliver
 * results after the new Fragment or Activity is available.
 *
 * @param repository the data source this ViewModel will fetch results from.
 */
class MainViewModel(private val repository: TitleRepository) : ViewModel() {

    companion object {
        /**
         * Factory for creating [MainViewModel]
         *
         * @param arg the repository to pass to [MainViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::MainViewModel)
    }

    /**
     * Request a snackbar to display a string.
     *
     * This variable is private because we don't want to expose MutableLiveData
     *
     * MutableLiveData allows anyone to set a value, and MainViewModel is the only
     * class that should be setting values.
     */
    private val _snackBar = MutableLiveData<String?>()

    /**
     * Request a snackbar to display a string.
     */
    val snackbar: LiveData<String?>
        get() = _snackBar

    /**
     * Update title text via this LiveData
     */
    val title = repository.title

    private val _spinner = MutableLiveData<Boolean>(false)

    /**
     * Show a loading spinner if true
     */
    val spinner: LiveData<Boolean>
        get() = _spinner

    /**
     * Count of taps on the screen
     */
    private var tapCount = 0

    /**
     * LiveData with formatted tap count.
     */
    private val _taps = MutableLiveData<String>("$tapCount taps")

    /**
     * Public view of tap live data.
     */
    val taps: LiveData<String>
        get() = _taps

    /**
     * Respond to onClick events by refreshing the title.
     *
     * The loading spinner will display until a result is returned, and errors will trigger
     * a snackbar.
     */
    fun onMainViewClicked() {
        refreshTitle()
        updateTaps()
    }

    /**
     * Wait one second then update the tap count.
     */
    private fun updateTaps() {

        // 아래 코드와의 차이점
        // onCleared 호출 시 자동으로 취소됨
        // 해당 Job이 취소되면, 해당 Scope,Job의 모든 코루틴이 취소된다.
        // viewModelScope는 기본적으로 Dispatchers.Main.immediate 를 사용한다.
        // 즉, 해당 코루틴은 메인 스레드에서 실행된다. (아래 코드는 BACKGROUND 에서 실행됨 !)
        // sleep 대신 suspend 함수인 delay을 사용한다.

        // launch a coroutine in viewModelScope
        viewModelScope.launch {
            tapCount++
            // suspend this coroutine for one second
            // delay 는 메인스레드에서 실행되더라도, 스레드를 blocking 하지 않는다.
            // 왼쪽의 -> 아이콘은 suspend 함수인 것을 의미한다.
            delay(1_000)
            // resume in the main dispatcher
            // _snackbar.value can be called directly from main thread
            _taps.postValue("$tapCount taps")
        }


//        tapCount++
//        BACKGROUND.submit {
//            Thread.sleep(1_000)
//            _taps.postValue("${tapCount} taps")
//        }

        // 아래와 같이 BACKGROUND.submit 를 주석처리하면, 스피너가 동작하지 않음
        // 이유 : 메인 스레드가 sleep 하기 때문에, UI가 freeze
//        Thread.sleep(1_000)
//        _taps.postValue("${tapCount} taps")
    }

    /**
     * Called immediately after the UI shows the snackbar.
     */
    fun onSnackbarShown() {
        _snackBar.value = null
    }

    /**
     * Refresh the title, showing a loading spinner while it refreshes and errors via snackbar.
     */
    fun refreshTitle() {
        launchDataLoad {
            repository.refreshTitle()
        }
    }
//    fun refreshTitle() {
//        // when the user moves away from this screen the work started by this coroutine will automatically be cancelled
//        viewModelScope.launch {
//            try {
//                _spinner.value = true
//                repository.refreshTitle()
//                // rely on the built-in language support for error handling instead of building custom error handling for every callback.
//                // Callback의 단점 중 하나 였던 부분 (일부 언어 기능을 지원하지 못한다. Exception 처리)
//            } catch (error: TitleRefreshError) {
//                    // 일반 함수처럼 try/catch로 예외처리를 할 수 있다.
//                    // 만약에 코루틴에서 에러가 발생하면, 코루틴은 기본적으로 부모 코루틴을 취소한다. 다른 자식들에게 전파됨
//                _snackBar.value = error.message
//            } finally {
//                _spinner.value = false
//            }
//
//        // By default, uncaught exceptions (catch로 못잡은 에러들)
//        // JVM에서 스레드의 포착되지 않은 예외 처리기로 전송된다.
//        // CoroutineExceptionHandler를 제공하여 이 동작을 사용자 정의할 수 있다.
//        }
//    }

    /**
     * abstracting the logic around showing a loading spinner and showing errors
     */
    // 람다를 적절히 사용하면, 중복 코드 제거하고 캡슐화하는데에 도움을 줄 수 있다.
    private fun launchDataLoad(block: suspend () -> Unit): Job { // suspend 함수도 람수로 표현할 수 있다.
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
}
