package nexters.tuk.config

import org.quartz.spi.TriggerFiredBundle
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SpringBeanJobFactory

@Configuration
class QuartzConfig(
    private val applicationContext: ApplicationContext
) {
    @Bean
    fun schedulerFactoryBean(): SchedulerFactoryBean {
        val factory = SchedulerFactoryBean()
        factory.isAutoStartup = true
        factory.setJobFactory(AutowiringJobFactory(applicationContext.autowireCapableBeanFactory))
        return factory
    }
}

class AutowiringJobFactory(
    private val beanFactory: AutowireCapableBeanFactory
) : SpringBeanJobFactory() {

    override fun createJobInstance(bundle: TriggerFiredBundle): Any {
        val job = super.createJobInstance(bundle)
        beanFactory.autowireBean(job)
        return job
    }
}

