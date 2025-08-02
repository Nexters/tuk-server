package nexters.tuk.job

import org.quartz.Job
import org.quartz.JobDetail
import org.quartz.Trigger

interface TukJob : Job {
    fun getTrigger(): Trigger
    fun getJobDetail(): JobDetail
}