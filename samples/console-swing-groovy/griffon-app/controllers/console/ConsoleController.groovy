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
package console

import griffon.core.artifact.GriffonController
import griffon.core.controller.ControllerAction
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor

import javax.annotation.Nonnull
import javax.inject.Inject

@ArtifactProviderFor(GriffonController)
class ConsoleController {
    @MVCMember @Nonnull
    ConsoleModel model                                       //<1>

    @Inject
    Evaluator evaluator                                      //<2>

    @ControllerAction
    void executeScript() {                                   //<3>
        model.enabled = false
        def result
        try {
            result = evaluator.evaluate(model.scriptSource)  //<4>
        } finally {
            model.enabled = true
            model.scriptResult = result                      //<5>
        }
    }
}
