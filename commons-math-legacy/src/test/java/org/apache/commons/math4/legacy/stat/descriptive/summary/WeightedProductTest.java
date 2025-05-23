/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math4.legacy.stat.descriptive.summary;

import org.apache.commons.math4.legacy.stat.descriptive.WeightedEvaluation;
import org.apache.commons.math4.legacy.stat.descriptive.WeightedEvaluationAbstractTest;

/**
 * Test cases for the {@link WeightedProduct} class.
 */
class WeightedProductTest extends WeightedEvaluationAbstractTest {

    @Override
    public WeightedEvaluation getWeightedEvaluation() {
        return WeightedProduct.getInstance();
    }

    @Override
    public double expectedValue() {
        return weightedProduct;
    }

    @Override
    public double expectedUnweightedValue() {
        return product;
    }

    @Override
    public double emptyValue() {
        return 1;
    }

    @Override
    public double getTolerance() {
        return 1e9; //sic -- big absolute error due to only 15 digits of accuracy in double
    }
}
