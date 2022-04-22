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
import androidx.lifecycle.map

/**
 * TitleRepository provides an interface to fetch a title or request a new one be generated.
 *
 * Repository modules handle data operations. They provide a clean API so that the rest of the app
 * can retrieve this data easily. They know where to get the data from and what API calls to make
 * when data is updated. You can consider repositories to be mediators between different data
 * sources, in our case it mediates between a network API and an offline database cache.
 */
class TitleRepository(val network: MainNetwork, val titleDao: TitleDao) {

    /**
     * [LiveData] to load title.
     *
     * This is the main interface for loading a title. The title will be loaded from the offline
     * cache.
     *
     * Observing this will not cause the title to be refreshed, use [TitleRepository.refreshTitleWithCallbacks]
     * to refresh the title.
     */
    val title: LiveData<String?> = titleDao.titleLiveData.map { it?.title }


//    suspend fun refreshTitle() {
//        // interact with *blocking* network and IO calls from a coroutine
//        // (IO dispatcher의) 스레드를 아무튼 blocking 하긴 한다.
//        // -> 하지만, 아래 withContext 블럭이 완료 될 때까지, suspend 함수인 refreshTitle의 caller 코루틴은 일시 중단 (suspend)된다.
//        // 즉, caller 코루틴을 실행 중인 스레드에게 (아마 main dispatcher의 메인 스레드에게) CPU 제어권이 바로 리턴되어 메인 스레드가 다른 작업을 할 수 있게한다.
//        withContext(Dispatchers.IO) {
//            // withContext는 caller Dispatcher에게 결과 값을 리턴 -> 콜백 함수를 호출하지 않아도 됨 !
//            // 코루틴을 사용하여, caller가 콜백을 넘겨주지 않아도 됨 !
//            val result = try {
//                // Make network request using a
//                // *blocking call
//                network.fetchNextTitle().execute()
//            } catch (cause: Throwable) {
//                // If the network throws an exception, inform the caller
//                throw TitleRefreshError("Unable to refresh title", cause)
//            }
//
//            if (result.isSuccessful) {
//                // Save it to database using a
//                // *blocking call*
//                titleDao.insertTitle(Title(result.body()!!))
//            } else {
//                // If it's not successful, inform the callback of the error
//                throw TitleRefreshError("Unable to refresh title", null)
//            }
//        }
//    }

    suspend fun refreshTitle() {
        // main-safe한 함수를 호출하기 위해 굳이 withContext를 사용하지 않아도 된다. 이미 main-safe하기 때문이다.
        // 또한 관례적으로, 작성한 suspend 함수가 main-safe한지 확인하여야한다.
        // main-safe하다면 Dispatchers.Main을 포함한 모든 디스패처에서 안전하게 호출할 수 있다.
        try {
            // Make network request using a blocking call
            val result = network.fetchNextTitle()
            titleDao.insertTitle(Title(result))
        } catch (cause: Throwable) {
            // If anything throws an exception, inform the caller
            throw TitleRefreshError("Unable to refresh title", cause)
        }
    }

    /**
     * Refresh the current title and save the results to the offline cache.
     *
     * This method does not return the new title. Use [TitleRepository.title] to observe
     * the current tile.
     */

    // * not used anymore *

//    fun refreshTitleWithCallbacks(titleRefreshCallback: TitleRefreshCallback) {
//        // This request will be run on a background thread by retrofit
//        BACKGROUND.submit {
//            try {
//                // Make network request using a blocking call
//                val result = network.fetchNextTitle().execute()
//                if (result.isSuccessful) {
//                    // Save it to database
//                    titleDao.insertTitle(Title(result.body()!!))
//                    // Inform the caller the refresh is completed
//                    titleRefreshCallback.onCompleted()
//                } else {
//                    // If it's not successful, inform the callback of the error
//                    titleRefreshCallback.onError(
//                        TitleRefreshError("Unable to refresh title", null)
//                    )
//                }
//            } catch (cause: Throwable) {
//                // If anything throws an exception, inform the caller
//                titleRefreshCallback.onError(
//                    TitleRefreshError("Unable to refresh title", cause)
//                )
//            }
//        }
//    }
}

/**
 * Thrown when there was a error fetching a new title
 *
 * @property message user ready error message
 * @property cause the original cause of this exception
 */
class TitleRefreshError(message: String, cause: Throwable?) : Throwable(message, cause)


// In TitleRepository.kt the method refreshTitleWithCallbacks is implemented with a callback
// to communicate the loading and error state to the caller.
interface TitleRefreshCallback {
    fun onCompleted()
    fun onError(cause: Throwable)
}
