package com.accounts

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

/**
 * Base class for all integration tests that starts all dependencies using TestContainers library.
 *
 *
 * It's up to the extending class to decide if it starts full application using [SpringBootTest] or slices like [DataJpaTest].
 */
@ActiveProfiles("integration-test")
@ContextConfiguration(initializers = [Initializer::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class IntegrationTestParent

class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        val values = TestPropertyValues.empty()
        values.applyTo(configurableApplicationContext)
    }
}
