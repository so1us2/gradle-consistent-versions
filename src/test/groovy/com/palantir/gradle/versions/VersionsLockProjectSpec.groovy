/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.gradle.versions

import nebula.test.ProjectSpec
import org.gradle.api.provider.ListProperty

class VersionsLockProjectSpec extends ProjectSpec {

    String getPluginName() {
        return "com.palantir.consistent-versions"
    }

    def 'apply does not throw exceptions'() {
        when:
        project.apply plugin: pluginName

        then:
        noExceptionThrown()
    }

    def 'apply is idempotent'() {
        when:
        project.apply plugin: pluginName
        project.apply plugin: pluginName

        then:
        noExceptionThrown()
    }

    def 'list property behaviour'() {
        ListProperty<Integer> list = project.objects.listProperty(Integer)
        ListProperty<Integer> list2 = project.objects.listProperty(Integer)
        list.addAll([1, 2])
        list2.set(list)

        when:
        list2.add(4)

        then:
        list.get() == [1, 2]
        list2.get() == [1, 2, 4]

        when:
        list.add(3)

        then:
        list.get() == [1, 2, 3]
        list2.get() == [1, 2, 3, 4]
    }
}
