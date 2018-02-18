/*
 * Copyright 2017 Martin Schvarcbacher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testframework.mosaik.models.measure;

import org.junit.Test;
import testframework.mosaik.dto.measure.Measure;
import testframework.mosaik.dto.measure.operators.AndOperand;
import testframework.mosaik.dto.measure.operators.GreaterThanOperand;
import testframework.mosaik.dto.measure.operators.LessThanOperand;
import testframework.mosaik.dto.measure.operators.OrOperand;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class OperandTest {

    @Test
    public void testLessThan() {
        Measure fifty = new Measure(50);
        Measure hundred = new Measure(100);

        LessThanOperand operand = new LessThanOperand(hundred);
        assertTrue(operand.isInRange(fifty));
    }

    @Test
    public void testGreaterThan() {
        Measure fifty = new Measure(50);
        Measure hundred = new Measure(100);

        GreaterThanOperand operand = new GreaterThanOperand(fifty);
        assertTrue(operand.isInRange(hundred));
    }

    @Test
    public void chainedAndOperatorsTest() {
        Measure ten = new Measure(10);
        Measure twenty = new Measure(20);
        Measure fifty = new Measure(50);
        Measure hundred = new Measure(100);
        Measure thousand = new Measure(1000);

        GreaterThanOperand greaterThanTwenty = new GreaterThanOperand(twenty); //x > 20
        LessThanOperand lessThanHundred = new LessThanOperand(hundred); // x < 100
        AndOperand andOperand = new AndOperand();
        andOperand.add(greaterThanTwenty);
        andOperand.add(lessThanHundred);

        assertTrue(andOperand.isInRange(fifty));
        assertFalse(andOperand.isInRange(thousand));
        assertFalse(andOperand.isInRange(ten));
    }

    @Test
    public void chainedOrOperatorsTest() {
        Measure ten = new Measure(10);
        Measure twenty = new Measure(20);
        Measure fifty = new Measure(50);
        Measure hundred = new Measure(100);
        Measure thousand = new Measure(1000);

        GreaterThanOperand greaterThanHundred = new GreaterThanOperand(hundred); //x>100
        LessThanOperand lessThanTwenty = new LessThanOperand(twenty); //x<20
        OrOperand orOperand = new OrOperand();
        orOperand.add(greaterThanHundred);
        orOperand.add(lessThanTwenty);

        assertFalse(orOperand.isInRange(fifty));
        assertTrue(orOperand.isInRange(thousand));
        assertTrue(orOperand.isInRange(ten));
    }


    @Test
    public void recursiveOperatorsTest() {
        Measure twenty = new Measure(20);
        Measure fifty = new Measure(50);
        Measure sixty = new Measure(60);
        Measure hundred = new Measure(100);
        Measure thousand = new Measure(1000);

        GreaterThanOperand greaterThanTwenty = new GreaterThanOperand(twenty); //x > 20
        LessThanOperand lessThanHundred = new LessThanOperand(hundred); // x < 100
        AndOperand andOperand = new AndOperand();
        andOperand.add(greaterThanTwenty);
        andOperand.add(lessThanHundred);


        AndOperand operand2 = new AndOperand();
        GreaterThanOperand greaterThanFifty = new GreaterThanOperand(fifty); // x > 50
        operand2.add(andOperand); // 20 < x < 100
        operand2.add(greaterThanFifty); // x > 50
        //===>   50 < x < 100

        assertTrue(operand2.isInRange(sixty));
        assertFalse(operand2.isInRange(thousand));
        assertFalse(operand2.isInRange(twenty));
    }

}
