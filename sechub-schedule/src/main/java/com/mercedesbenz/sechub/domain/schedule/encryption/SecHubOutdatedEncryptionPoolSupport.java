package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.SystemTimeProvider;

@Component
public class SecHubOutdatedEncryptionPoolSupport {

    /*
     * This is just an additional time buffer when calculating the maximum refresh
     * interval time
     */
    private static final int MAX_REFRESH_INTERVAL_ADDITIONAL_TIME_BUFFER_MILLISECONDS = 500;
    private static final int DEFAULT_ACCEPT_OUTDATED_POOL_IN_MILLISECONDS = 30 * 60 * 1000; // 30 minutes

    @MustBeDocumented("The maximum amount of milliseconds an outdated encryption pool is still accepted in refresh phase")
    @Value("${sechub.schedule.encryption.refresh.accept-outdated.milliseconds:" + DEFAULT_ACCEPT_OUTDATED_POOL_IN_MILLISECONDS + "}")
    long acceptOutdatedEncryptionPoolInMilliseconds;

    // documented in ScheduleRefreshEncryptionServiceSetupTriggerService
    @Value(ScheduleRefreshEncryptionServiceSetupTriggerService.SPRING_VALUE_FIXED_DELAY_MILLISECONDS)
    long encryptionRefreshFixedDelayInMilliseconds;

    // documented in ScheduleRefreshEncryptionServiceSetupTriggerService
    @Value(ScheduleRefreshEncryptionServiceSetupTriggerService.SPRING_VALUE_INITIAL_DELAY_MILLISECONDS)
    long encryptionRefreshInitialDelayInMilliseconds;

    @Autowired
    SystemTimeProvider systemtimeProvider;

    @Autowired
    ScheduleCipherPoolDataProvider poolDataProvider;

    @Autowired
    ScheduleLatestCipherPoolDataCalculator latestCipherPoolDataCalculator;

    /**
     * As long as an outdated encryption pool still needs to run (e.g. while a
     * rolling update/ deployment is done with K8s and the old server is still up
     * and running and does create jobs...).
     *
     * The method will check if the maximum allowed time for an outdated encryption
     * pool has been reached by following algorithm:
     * <ul>
     * <li>the creation time stamp of latest cipher pool data entry from database is
     * fetched</li>
     * <li>the difference from current time and the creation time is calculated</li>
     * <li>if the difference is bigger than the allowed maximum value this method
     * returns false, otherwise true</li>
     * </ul>
     *
     * @return <code>true</code> as long as still acceptable, otherwise
     *         <code>false</code>
     */
    public boolean isOutdatedEncryptionStillAllowedOnThisClusterMember() {
        Long latestPoolDataWasCreatedMillisecondsBefore = calculateMillisecondsLatestPoolDataHasBeenCreated();
        if (latestPoolDataWasCreatedMillisecondsBefore == null) {
            /* no pool defined, means cannot determine - return false */
            return false;
        }
        return acceptOutdatedEncryptionPoolInMilliseconds > latestPoolDataWasCreatedMillisecondsBefore;
    }

    /**
     * This method is used to avoid the risk of race conditions when encryption pool
     * entries are still in use but wanted to be removed.
     * <h3>Example-Situation:</h3>
     *
     * <pre>
     * SecHub-Cluster member A: - SECRET_1 - SECRET_2 (new encryption pool entry PA, poolId = 2)
     *
     * SecHub-Cluster member B: - SECRET_1 (old encryption pool entry PB, poolId = 1)
     * </pre>
     *
     * SecHub B creates now a new job which is currently not stored in database but
     * with old encryption pool PB (poolId=1). In the mean time cluster member A
     * does not find any jobs which using pool id 1 and could delete now the
     * encryption pool entity with id 1...<br>
     * <br>
     * After this the member B would write the encrypted configuration with poolId=1
     * to database, because the encryption pool is still in memory and its latest
     * entry is pool id 1 -> after this member 2 is also new started with only
     * SECRET_2 settings..
     *
     * --> Now we have a bad situation: we would have a created job encrypted with
     * SECRET_1, but no possibility to handle the created job by SecHub any more.
     *
     *
     * <h3>Problem</h3>
     *
     * SecHub does not use a full blown event bus (like KAFKA) but a simple event
     * listener approach which works only inside one JVM, we have no possibility to
     * send cluster wide events to check there are no longer outdated encryption
     * pools used.
     *
     * <h3>Solution</h3> To handle the problem without a full blown event bus, we
     * ensure that outdated encryption pools can remain running only a dedicated
     * time. The time an outdated encryption pool is accepted is calculated by
     * {@link #isOutdatedEncryptionStillAllowedOnThisClusterMember()}.
     *
     * Means we ask here the question: "Was the maximum outdate time exceeded at the
     * last refresh interval?"
     *
     * <pre>
     *      Created                     Cluster wrong EP          Cluster wrong EP
     *        New      Refresh              possible:  Refresh           possible:
     *        Pool Id   Trigger                        Trigger
     * +-----------------+--------------------Y-----------+--------------------N-------------------
     *         |        Outdated                        Outdated               |
     *         |         |                                |                    |
     *         |         |----------------------------x   not                  |
     *         |            still allowed on cluster      allowed              |
     *         |            member                    (max time diff reached)  |
     *         |<------------------------------------------------------------->|
     *         |  (time from created to now)                                  (now)
     *         |
     *         |<------------------------------------------>X<-----------------|
     *         | (time from created to last refresh trigger)| (now-refresh trigger time)
     *                                                      |
     *                                                      |
     *                                                  timeOnLastRefreshTigger
     * </pre>
     *
     *
     * @return <code>true</code> when outdated encryption pool is possible in
     *         cluster, otherwise <code>false</code>
     */
    public boolean isOutdatedEncryptionPoolPossibleInCluster() {
        Long latestPoolDataWasCreatedMillisecondsBefore = calculateMillisecondsLatestPoolDataHasBeenCreated();
        if (latestPoolDataWasCreatedMillisecondsBefore == null) {
            /* no pool defined, means outdated is not possible */
            return false;
        }

        long maxRefreshIntervalTimeMillseconds = encryptionRefreshInitialDelayInMilliseconds + encryptionRefreshFixedDelayInMilliseconds
                + MAX_REFRESH_INTERVAL_ADDITIONAL_TIME_BUFFER_MILLISECONDS;

        long maxMillisecondsDifferenceToLastRefreshTigger = Math.abs(latestPoolDataWasCreatedMillisecondsBefore - maxRefreshIntervalTimeMillseconds);

        return acceptOutdatedEncryptionPoolInMilliseconds > maxMillisecondsDifferenceToLastRefreshTigger;
    }

    private Long calculateMillisecondsLatestPoolDataHasBeenCreated() {
        List<ScheduleCipherPoolData> allAvailablePoolData = poolDataProvider.ensurePoolDataAvailable();
        ScheduleCipherPoolData latest = latestCipherPoolDataCalculator.calculateLatestPoolData(allAvailablePoolData);
        if (latest == null) {
            return null;
        }
        LocalDateTime latestCreationTimeStamp = latest.getCreated();

        return Duration.between(latestCreationTimeStamp, systemtimeProvider.getNow()).toMillis();
    }
}
