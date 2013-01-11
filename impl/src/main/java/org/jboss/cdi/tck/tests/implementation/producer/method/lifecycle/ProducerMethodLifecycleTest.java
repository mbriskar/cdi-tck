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
package org.jboss.cdi.tck.tests.implementation.producer.method.lifecycle;

import static org.jboss.cdi.tck.cdi.Sections.CONTEXTUAL;
import static org.jboss.cdi.tck.cdi.Sections.PRODUCER_METHOD;
import static org.jboss.cdi.tck.cdi.Sections.PRODUCER_METHOD_LIFECYCLE;
import static org.jboss.cdi.tck.cdi.Sections.PRODUCER_OR_DISPOSER_METHODS_INVOCATION;
import static org.jboss.cdi.tck.cdi.Sections.SPECIALIZATION;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.IllegalProductException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.cdi.tck.AbstractTest;
import org.jboss.cdi.tck.shrinkwrap.WebArchiveBuilder;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecAssertions;
import org.jboss.test.audit.annotations.SpecVersion;
import org.testng.annotations.Test;

/**
 * NOTE May be able to get rid of some of the binding types if the producer method precedence question is resolved
 */
@SpecVersion(spec = "cdi", version = "20091101")
public class ProducerMethodLifecycleTest extends AbstractTest {
    private AnnotationLiteral<Pet> PET_LITERAL = new AnnotationLiteral<Pet>() {
    };
    private AnnotationLiteral<FirstBorn> FIRST_BORN_LITERAL = new AnnotationLiteral<FirstBorn>() {
    };
    private AnnotationLiteral<Fail> FAIL_LITERAL = new AnnotationLiteral<Fail>() {
    };
    private AnnotationLiteral<Null> NULL_LITERAL = new AnnotationLiteral<Null>() {
    };

    @Deployment
    public static WebArchive createTestArchive() {
        return new WebArchiveBuilder().withTestClassPackage(ProducerMethodLifecycleTest.class).withBeansXml("beans.xml")
                .build();
    }

    @Test
    @SpecAssertion(section = PRODUCER_METHOD_LIFECYCLE, id = "ea")
    public void testProducerMethodBeanCreate() {
        PreferredSpiderProducer.reset();
        Bean<Tarantula> tarantulaBean = getBeans(Tarantula.class, PET_LITERAL).iterator().next();
        CreationalContext<Tarantula> tarantulaCc = getCurrentManager().createCreationalContext(tarantulaBean);
        Tarantula tarantula = tarantulaBean.create(tarantulaCc);
        assert PreferredSpiderProducer.getTarantulaCreated() == tarantula;
        assert PreferredSpiderProducer.getInjectedWeb() != null;
        assert PreferredSpiderProducer.getInjectedWeb().isDestroyed();
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = PRODUCER_METHOD_LIFECYCLE, id = "ea") })
    public void testProducerMethodInvokedOnCreate() {
        Bean<SpiderEgg> eggBean = getBeans(SpiderEgg.class, FIRST_BORN_LITERAL).iterator().next();
        CreationalContext<SpiderEgg> eggCc = getCurrentManager().createCreationalContext(eggBean);
        assert eggBean.create(eggCc) != null;
    }

    @Test
    @SpecAssertion(section = PRODUCER_METHOD, id = "j")
    public void testWhenApplicationInvokesProducerMethodParametersAreNotInjected() {
        try {
            getInstanceByType(BrownRecluse.class).layAnEgg(null);
        } catch (AssertionError e) {
            return;
        }

        assert false : "The BeanManager should not have been injected into the producer method";
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = PRODUCER_OR_DISPOSER_METHODS_INVOCATION, id = "c"), @SpecAssertion(section = SPECIALIZATION, id = "cb") })
    public void testProducerMethodFromSpecializedBeanUsed() {
        SpiderProducer.reset();
        PreferredSpiderProducer.reset();
        Bean<Tarantula> spiderBean = getBeans(Tarantula.class, PET_LITERAL).iterator().next();
        CreationalContext<Tarantula> spiderBeanCc = getCurrentManager().createCreationalContext(spiderBean);
        Tarantula tarantula = spiderBean.create(spiderBeanCc);
        assert PreferredSpiderProducer.getTarantulaCreated() == tarantula;
        assert !SpiderProducer.isTarantulaCreated();
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = PRODUCER_METHOD_LIFECYCLE, id = "k") })
    public void testCreateReturnsNullIfProducerDoesAndDependent() {
        Bean<Spider> nullSpiderBean = getBeans(Spider.class, NULL_LITERAL).iterator().next();
        CreationalContext<Spider> nullSpiderBeanCc = getCurrentManager().createCreationalContext(nullSpiderBean);
        assert nullSpiderBean.create(nullSpiderBeanCc) == null;
    }

    @Test(expectedExceptions = IllegalProductException.class)
    @SpecAssertions({ @SpecAssertion(section = PRODUCER_METHOD_LIFECYCLE, id = "l") })
    public void testCreateFailsIfProducerReturnsNullAndNotDependent() {
        Bean<PotatoChip> potatoChipBean = getBeans(PotatoChip.class, NULL_LITERAL).iterator().next();
        assert potatoChipBean != null;

        CreationalContext<PotatoChip> chipCc = getCurrentManager().createCreationalContext(potatoChipBean);
        potatoChipBean.create(chipCc);
        assert false;
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = PRODUCER_METHOD_LIFECYCLE, id = "ma"), @SpecAssertion(section = PRODUCER_METHOD_LIFECYCLE, id = "r") })
    public void testProducerMethodBeanDestroy() {
        PreferredSpiderProducer.reset();
        Set<Bean<?>> beans = getCurrentManager().getBeans(Tarantula.class, PET_LITERAL);
        Bean<?> bean = getCurrentManager().resolve(beans);
        assert bean.getBeanClass().equals(PreferredSpiderProducer.class);
        assert bean.getTypes().contains(Tarantula.class);
        Bean<Tarantula> tarantulaBean = (Bean<Tarantula>) bean;
        CreationalContext<Tarantula> tarantulaCc = getCurrentManager().createCreationalContext(tarantulaBean);
        Tarantula tarantula = tarantulaBean.create(tarantulaCc);
        PreferredSpiderProducer.resetInjections();
        tarantulaBean.destroy(tarantula, tarantulaCc);
        assert PreferredSpiderProducer.getTarantulaDestroyed() == tarantula;
        assert PreferredSpiderProducer.isDestroyArgumentsSet();
        assert PreferredSpiderProducer.getInjectedWeb() != null;
        assert PreferredSpiderProducer.getInjectedWeb().isDestroyed();
    }

    @Test(expectedExceptions = FooException.class)
    @SpecAssertions({ @SpecAssertion(section = CONTEXTUAL, id = "a0") })
    public void testCreateRethrowsUncheckedException() {
        Bean<Ship> shipBean = getBeans(Ship.class, FAIL_LITERAL).iterator().next();
        CreationalContext<Ship> shipCc = getCurrentManager().createCreationalContext(shipBean);
        shipBean.create(shipCc);
        assert false;
    }

    @Test(expectedExceptions = CreationException.class)
    @SpecAssertions({ @SpecAssertion(section = CONTEXTUAL, id = "a0") })
    public void testCreateWrapsCheckedExceptionAndRethrows() {
        Bean<Lorry> lorryBean = getBeans(Lorry.class, FAIL_LITERAL).iterator().next();
        CreationalContext<Lorry> lorryCc = getCurrentManager().createCreationalContext(lorryBean);
        lorryBean.create(lorryCc);
        assert false;
    }
}
