/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2008-2017 the original author or authors.
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
package griffon.pivot.support

import griffon.core.RunnableWithArgs
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class PivotActionSpec extends Specification {
    void 'Create and execute an action'() {
        given:
        boolean invoked = false
        RunnableWithArgs runnable = { invoked = true }
        PivotAction action = new PivotAction()
        action.runnable = runnable

        when:
        action.name = 'action'
        action.description = 'random description'
        action.perform(null)

        then:
        invoked
        action.name == 'action'
        action.description == 'random description'
        action.runnable == runnable
        action.toString() == 'Action[action, random description]'
    }
}
