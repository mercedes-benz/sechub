package com.mercedesbenz.sechub.domain.schedule.encryption;

import static java.time.LocalDateTime.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.mercedesbenz.sechub.sharedkernel.SystemTimeProvider;

class SecHubOutdatedEncryptionPoolSupportTest {

    private SecHubOutdatedEncryptionPoolSupport supportToTest;
    private SystemTimeProvider systemtimeProvider;
    private ScheduleCipherPoolDataProvider poolDataProvider;
    private ScheduleLatestCipherPoolDataCalculator latestCipherPoolDataCalculator;

    @BeforeEach
    void beforeEach() {
        systemtimeProvider = mock(SystemTimeProvider.class);
        poolDataProvider = mock(ScheduleCipherPoolDataProvider.class);
        latestCipherPoolDataCalculator = mock(ScheduleLatestCipherPoolDataCalculator.class);

        supportToTest = new SecHubOutdatedEncryptionPoolSupport();
        supportToTest.systemtimeProvider = systemtimeProvider;
        supportToTest.poolDataProvider = poolDataProvider;
        supportToTest.latestCipherPoolDataCalculator = latestCipherPoolDataCalculator;
    }

    @ParameterizedTest
    @ArgumentsSource(OutdatedEncryptionPoolOnThisClusterMemberAllowedArgumentProvider.class)
    @ArgumentsSource(OutdatedEncryptionPoolOnThisClusterMemberNotAllowedArgumentProvider.class)
    void outdated_encryption_on_cluster_member_NOT_allowed_when_no_latest_pooldata(LocalDateTime now, long outdatedAcceptedMillis,
            LocalDateTime latestPoolIdCreationTimestamp) throws Exception {

        /* prepare */
        when(systemtimeProvider.getNow()).thenReturn(now);

        List<ScheduleCipherPoolData> list = List.of(); // empty list...
        when(poolDataProvider.ensurePoolDataAvailable()).thenReturn(list);
        when(latestCipherPoolDataCalculator.calculateLatestPoolData(list)).thenReturn(null);

        supportToTest.acceptOutdatedEncryptionPoolInMilliseconds = outdatedAcceptedMillis;

        /* execute */
        boolean result = supportToTest.isOutdatedEncryptionStillAllowedOnThisClusterMember();

        /* test */
        assertThat(result).isFalse();

    }

    @ParameterizedTest
    @ArgumentsSource(OutdatedEncryptionPoolOnThisClusterMemberNotAllowedArgumentProvider.class)
    void outdated_encryption_on_cluster_member_NOT_allowed(LocalDateTime now, long outdatedAcceptedMillis,
            LocalDateTime latestPoolIdCreationTimestamp) throws Exception {

        /* prepare */
        when(systemtimeProvider.getNow()).thenReturn(now);

        ScheduleCipherPoolData latestPoolData = mock(ScheduleCipherPoolData.class);
        when(latestPoolData.getCreated()).thenReturn(latestPoolIdCreationTimestamp);
        List<ScheduleCipherPoolData> list = List.of(latestPoolData);
        when(poolDataProvider.ensurePoolDataAvailable()).thenReturn(list);
        when(latestCipherPoolDataCalculator.calculateLatestPoolData(list)).thenReturn(latestPoolData);

        supportToTest.acceptOutdatedEncryptionPoolInMilliseconds = outdatedAcceptedMillis;

        /* execute */
        boolean result = supportToTest.isOutdatedEncryptionStillAllowedOnThisClusterMember();

        /* test */
        assertThat(result).isFalse();

    }

    @ParameterizedTest
    @ArgumentsSource(OutdatedEncryptionPoolOnThisClusterMemberAllowedArgumentProvider.class)
    void outdated_encryption_on_cluster_member_allowed(LocalDateTime now, long outdatedAcceptedMillis,
            LocalDateTime latestPoolIdCreationTimestamp) throws Exception {

        /* prepare */
        when(systemtimeProvider.getNow()).thenReturn(now);

        ScheduleCipherPoolData latestPoolData = mock(ScheduleCipherPoolData.class);
        when(latestPoolData.getCreated()).thenReturn(latestPoolIdCreationTimestamp);
        List<ScheduleCipherPoolData> list = List.of(latestPoolData);
        when(poolDataProvider.ensurePoolDataAvailable()).thenReturn(list);
        when(latestCipherPoolDataCalculator.calculateLatestPoolData(list)).thenReturn(latestPoolData);

        supportToTest.acceptOutdatedEncryptionPoolInMilliseconds = outdatedAcceptedMillis;

        /* execute */
        boolean result = supportToTest.isOutdatedEncryptionStillAllowedOnThisClusterMember();

        /* test */
        assertThat(result).isTrue();

    }

    @ParameterizedTest
    @ArgumentsSource(OutdatedEncryptionPoolInClusterPossibleArgumentProvider.class)
    @ArgumentsSource(OutdatedEncryptionPoolInClusterNotPossibleArgumentProvider.class)
    void outdated_encryption_pool_in_cluster_NOT_possible_when_no_latest_pooldata(LocalDateTime now, long outdatedAcceptedMillis,
            LocalDateTime latestPoolIdCreationTimestamp, long refreshIntervalMillis, long refreshInitialDelayMillis) throws Exception {

        /* prepare */
        when(systemtimeProvider.getNow()).thenReturn(now);

        List<ScheduleCipherPoolData> list = List.of(); // empty list...
        when(poolDataProvider.ensurePoolDataAvailable()).thenReturn(list);
        when(latestCipherPoolDataCalculator.calculateLatestPoolData(list)).thenReturn(null);

        supportToTest.acceptOutdatedEncryptionPoolInMilliseconds = outdatedAcceptedMillis;
        supportToTest.encryptionRefreshFixedDelayInMilliseconds = refreshIntervalMillis;
        supportToTest.encryptionRefreshInitialDelayInMilliseconds = refreshInitialDelayMillis;

        /* execute */
        boolean result = supportToTest.isOutdatedEncryptionPoolPossibleInCluster();

        /* test */
        assertThat(result).isFalse();

    }

    @ParameterizedTest
    @ArgumentsSource(OutdatedEncryptionPoolInClusterNotPossibleArgumentProvider.class)
    void outdated_encryption_pool_in_cluster__NOT_possible(LocalDateTime now, long outdatedAcceptedMillis,
            LocalDateTime latestPoolIdCreationTimestamp, long refreshIntervalMillis, long refreshInitialDelayMillis) throws Exception {

        /* prepare */
        when(systemtimeProvider.getNow()).thenReturn(now);

        ScheduleCipherPoolData latestPoolData = mock(ScheduleCipherPoolData.class);
        when(latestPoolData.getCreated()).thenReturn(latestPoolIdCreationTimestamp);
        List<ScheduleCipherPoolData> list = List.of(latestPoolData);
        when(poolDataProvider.ensurePoolDataAvailable()).thenReturn(list);
        when(latestCipherPoolDataCalculator.calculateLatestPoolData(list)).thenReturn(latestPoolData);

        supportToTest.acceptOutdatedEncryptionPoolInMilliseconds = outdatedAcceptedMillis;
        supportToTest.encryptionRefreshFixedDelayInMilliseconds = refreshIntervalMillis;
        supportToTest.encryptionRefreshInitialDelayInMilliseconds = refreshInitialDelayMillis;

        /* execute */
        boolean result = supportToTest.isOutdatedEncryptionPoolPossibleInCluster();

        /* test */
        assertThat(result).isFalse();

    }

    @ParameterizedTest
    @ArgumentsSource(OutdatedEncryptionPoolInClusterPossibleArgumentProvider.class)
    void outdated_encryption_pool_in_cluster__possible(LocalDateTime now, long outdatedAcceptedMillis,
            LocalDateTime latestPoolIdCreationTimestamp, long refreshIntervalMillis, long refreshInitialDelayMillis) throws Exception {

        /* prepare */
        when(systemtimeProvider.getNow()).thenReturn(now);

        ScheduleCipherPoolData latestPoolData = mock(ScheduleCipherPoolData.class);
        when(latestPoolData.getCreated()).thenReturn(latestPoolIdCreationTimestamp);
        List<ScheduleCipherPoolData> list = List.of(latestPoolData);
        when(poolDataProvider.ensurePoolDataAvailable()).thenReturn(list);
        when(latestCipherPoolDataCalculator.calculateLatestPoolData(list)).thenReturn(latestPoolData);

        supportToTest.acceptOutdatedEncryptionPoolInMilliseconds = outdatedAcceptedMillis;
        supportToTest.encryptionRefreshFixedDelayInMilliseconds = refreshIntervalMillis;
        supportToTest.encryptionRefreshInitialDelayInMilliseconds = refreshInitialDelayMillis;

        /* execute */
        boolean result = supportToTest.isOutdatedEncryptionPoolPossibleInCluster();

        /* test */
        assertThat(result).isTrue();

    }

    static class OutdatedEncryptionPoolOnThisClusterMemberAllowedArgumentProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            /* @formatter:off */
            return Stream.of(
                    /*create pool entry                now
                     * |------120s----------------------|
                     * |                                |
                     * |------------------------ accept outdated >120s--->X
                     */
                    Arguments.of(now(), acceptOutdated(121),   now().minusSeconds(120)),
                    Arguments.of(now(), acceptOutdated(210),   now().minusSeconds(120)),
                    Arguments.of(now(), acceptOutdated(10000), now().minusSeconds(120))
                    );
            /* @formatter:on */

        }

    }

    static class OutdatedEncryptionPoolOnThisClusterMemberNotAllowedArgumentProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            /* @formatter:off */
            return Stream.of(
                    /*create pool entry                now
                     * |------120s----------------------|
                     * |                                |
                     * |----- accept outdated < 120s->X
                     */
                    Arguments.of(now(), acceptOutdated(119), now().minusSeconds(120)),
                    Arguments.of(now(), acceptOutdated(21),  now().minusSeconds(120)),
                    Arguments.of(now(), acceptOutdated(0),   now().minusSeconds(120))
                    );
            /* @formatter:on */

        }

    }

    static class OutdatedEncryptionPoolInClusterPossibleArgumentProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            /* @formatter:off */
            return Stream.of(
                    /*create pool entry                now
                     * |------20s-----------------------|
                     * |               |<--5s+1s(+1)--->| (7s)
                     * |<-----13s----->|
                     * |               V
                     * |              last refresh point (calculated)
                     *  <------- > 13----> acceptance time is bigger -> still possible
                     */
                    Arguments.of(now(), acceptOutdated(14), now().minusSeconds(20), refreshInterval(5), refresInitialDelay(1)),
                    Arguments.of(now(), acceptOutdated(15), now().minusSeconds(20), refreshInterval(5), refresInitialDelay(1)),
                    Arguments.of(now(), acceptOutdated(100), now().minusSeconds(20), refreshInterval(5), refresInitialDelay(1)),

                    /*create pool entry                now
                     * |------60s-----------------------|
                     * |               |<--25s+10s(+1)->| (36s)
                     * |<-----24s----->|
                     * |               V
                     * |              last refresh point (calculated)
                     *  <------- > 24----> acceptance time is bigger -> still possible
                     */
                    Arguments.of(now(), acceptOutdated(25), now().minusSeconds(60), refreshInterval(25), refresInitialDelay(10)),
                    Arguments.of(now(), acceptOutdated(37), now().minusSeconds(60), refreshInterval(25), refresInitialDelay(10)),
                    Arguments.of(now(), acceptOutdated(60), now().minusSeconds(60), refreshInterval(25), refresInitialDelay(10)),

                    /*create pool entry                now
                     * |------120s----------------------|
                     * |               |<--15s+3s(+1)- >| (19s)
                     * |<-----101s---->|
                     * |               V
                     * |              last refresh point (calculated)
                     *  <------- > 101----> acceptance time is bigger -> still possible
                     */
                    Arguments.of(now(), acceptOutdated(102),    now().minusSeconds(120), refreshInterval(15), refresInitialDelay(3)),
                    Arguments.of(now(), acceptOutdated(210),    now().minusSeconds(120), refreshInterval(15), refresInitialDelay(3)),
                    Arguments.of(now(), acceptOutdated(10000), now().minusSeconds(120), refreshInterval(15), refresInitialDelay(3))
                    );
            /* @formatter:on */

        }

    }

    static class OutdatedEncryptionPoolInClusterNotPossibleArgumentProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            /* @formatter:off */
            return Stream.of(
                    /*create pool entry                now
                     * |------20s-----------------------|
                     * |               |<--5s+1s(+1)--->|
                     * |<-----13s----->|
                     * |               V
                     * |              last refresh point (calculated)
                     *  <----- < 13--> acceptance time is lower -> NOT possible
                     *
                     */
                    Arguments.of(now(), acceptOutdated(12), now().minusSeconds(20), refreshInterval(5), refresInitialDelay(1)),
                    Arguments.of(now(), acceptOutdated(11), now().minusSeconds(20), refreshInterval(5), refresInitialDelay(1)),
                    Arguments.of(now(), acceptOutdated(10), now().minusSeconds(20), refreshInterval(5), refresInitialDelay(1)),

                    /*create pool entry                now
                     * |------60s-----------------------|
                     * |               |<--25s+10s(+1)->| (36s)
                     * |<-----24s----->|
                     * |               V
                     * |              last refresh point (calculated)
                     *  <------- < 24----> acceptance time is lower -> NOT possible
                     */
                    Arguments.of(now(), acceptOutdated(23), now().minusSeconds(60), refreshInterval(25), refresInitialDelay(10)),
                    Arguments.of(now(), acceptOutdated(12), now().minusSeconds(60), refreshInterval(25), refresInitialDelay(10)),
                    Arguments.of(now(), acceptOutdated(1), now().minusSeconds(60), refreshInterval(25), refresInitialDelay(10)),

                    /*create pool entry                now
                     * |------120s----------------------|
                     * |               |<--15s+3s(+1)- >| (19s)
                     * |<-----101s---->|
                     * |               V
                     * |              last refresh point (calculated)
                     *  <------- < 101----> acceptance time is lower -> NOT possible
                     */
                    Arguments.of(now(), acceptOutdated(100), now().minusSeconds(120), refreshInterval(15), refresInitialDelay(3)),
                    Arguments.of(now(), acceptOutdated(18), now().minusSeconds(120), refreshInterval(15), refresInitialDelay(3)),
                    Arguments.of(now(), acceptOutdated(10), now().minusSeconds(120), refreshInterval(15), refresInitialDelay(3)),
                    Arguments.of(now(), acceptOutdated(0), now().minusSeconds(120), refreshInterval(15), refresInitialDelay(3)),

                    /*                           create pool entry                 now
                     *                             v                                v
                     *                             |------1s------------------------|
                     *                             |         <--21s+5s(+1)- >| (27s)|
                     *      -----------------------|<-----1s----------------------->|
                     *      |
                     * |    V (26 seconds before creation time) (-26s)
                     * |    last refresh point (calculated)
                     *  <------- < -26----> acceptance time is lower -> NOT possible
                     */
                    Arguments.of(now(), acceptOutdated(10), now().minusSeconds(1), refreshInterval(21), refresInitialDelay(5)),
                    Arguments.of(now(), acceptOutdated(25), now().minusSeconds(1), refreshInterval(21), refresInitialDelay(5))
             );
            /* @formatter:on */

        }

    }

    /***
     * Some syntax sugar for test data - define accepted outdated time in SECONDS
     *
     * @param seconds define seconds
     *
     * @return milliseconds
     */
    private static long acceptOutdated(long seconds) {
        return seconds * 1000;
    }

    /**
     * Some syntax sugar for test data - define refresh interval in SECONDS * @param
     * seconds define seconds
     *
     * @return milliseconds
     */
    private static long refreshInterval(long seconds) {
        return seconds * 1000;
    }

    /**
     * Some syntax sugar for test data - define refresh initial delay in SECONDS
     *
     * @param seconds define seconds
     * @return milliseconds
     */
    private static long refresInitialDelay(long seconds) {
        return seconds * 1000;
    }
}
