package nexters.tuk.job

import org.quartz.*
import org.springframework.scheduling.quartz.QuartzJobBean

abstract class AbstractJob<T : Trigger> : QuartzJobBean(), Job {

    companion object {
        private const val JOB_PREFIX = "Tuk-Job-"
        private const val TRIGGER_PREFIX = "Tuk-Trigger-"
    }

    fun getJobDetail(): JobDetail {
        return JobBuilder.newJob(this::class.java)
            .withIdentity(JOB_PREFIX + this::class.simpleName)
            .build()
    }

    fun getTrigger(): Trigger {
        return TriggerBuilder.newTrigger()
            .withSchedule(getScheduleBuilder())
            .withIdentity(TRIGGER_PREFIX + this::class.simpleName)
            .build()
    }

    protected abstract fun getScheduleBuilder(): ScheduleBuilder<T>
}
