// Copyright 2018 Life360, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.life360.falx.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by remon on 7/17/17.
 * Todo: How can we fake the context?
 * Currently not faking it, and using Mockito with a Mock Context to use this Module.
 */

@Module
public class FakeAppModule {

    Context appContext;

    public FakeAppModule(Context application) {
        appContext = application;
    }

    @Provides
    @Singleton
    Context providesApplication() {
        return appContext;
    }
}