/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.cdi.tck.tests.context.passivating.producer;

import static org.jboss.cdi.tck.cdi.Sections.PASSIVATION_CAPABLE;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.cdi.tck.AbstractTest;
import org.jboss.cdi.tck.shrinkwrap.WebArchiveBuilder;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecVersion;
import org.testng.annotations.Test;

/**
 * 
 * @author Martin Kouba
 */
@SpecVersion(spec = "cdi", version = "20091101")
public class ProducerWithPrimitiveFieldTypePassivationTest extends AbstractTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return new WebArchiveBuilder().withTestClass(ProducerWithPrimitiveFieldTypePassivationTest.class)
                .withClasses(AnswerFieldProducer.class, AnswerToTheUltimateQuestion.class, Universe.class).build();
    }

    @Inject
    Universe universe;

    @Test
    @SpecAssertion(section = PASSIVATION_CAPABLE, id = "daa")
    public void testProducerFieldWithPrimitiveType() {
        assert universe.getAnswer() == 42;
    }

}
