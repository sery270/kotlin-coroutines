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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.kotlincoroutines.fakes.MainNetworkFake
import com.example.android.kotlincoroutines.fakes.TitleDaoFake
import com.example.android.kotlincoroutines.main.utils.MainCoroutineScopeRule
import com.example.android.kotlincoroutines.main.utils.getValueForTest
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    // MainCoroutineScopeRule (custom rule)
    // kotlinx-coroutines-test의 TestCoroutineDispatcher를 사용하도록
    // Dispatchers.Main을 구성한다.

    // 테스트를 위해 virtual-clock 향상
    // 단위 테스트에서 Dispatchers.Main 를 사용하게 함

    // MainCoroutineScopeRule lets you pause, resume, or control the execution of coroutines
    // that are launched on the Dispatchers.Main.
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    // InstantTaskExecutorRule (JUnit rule)
    // 각 작업을 동기적으로 실행하도록
    // LiveData 를 구성한다.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var subject: MainViewModel

    @Before
    fun setup() {
        subject = MainViewModel(
                TitleRepository(
                        MainNetworkFake("OK"),
                        TitleDaoFake("initial")
                ))
    }

    @Test
    fun whenMainClicked_updatesTaps() {
        subject.onMainViewClicked()
        Truth.assertThat(subject.taps.getValueForTest()).isEqualTo("0 taps")
        coroutineScope.advanceTimeBy(1000)
        Truth.assertThat(subject.taps.getValueForTest()).isEqualTo("1 taps")
    }
}