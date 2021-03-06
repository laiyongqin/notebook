import efficiency.Concurrent
import efficiency.FileIO
import efficiency.NetIO
import efficiency.AlgComplexity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Connection

@ContextConfiguration(locations = "classpath:db.xml")
class programEfficiencyTest extends Specification {

    NetIO netIO = new NetIO()

    @Autowired
    @Qualifier(value = "localConnection")
    Connection connection

    @Unroll
    def "algComplexityCost test"() {
        when:
        long quadXSpendTime = AlgComplexity.calQuadXSpendTime(x)
        long xSpendTime = AlgComplexity.calXSpendTime(x)
        long logXSpendTime = AlgComplexity.calLogXSpendTime(x)

        then:
        println("quadXSpendTime: " + quadXSpendTime)
        println("xSpendTime: " + xSpendTime)
        println("logXSpendTime: " + logXSpendTime)

        where:
        x       | _
        10000L  | _
        100000L | _
    }

    @Unroll
    def "netIOCost test"() {
        when:
        netIO.calOnceIOCost(1)
        long onceCost = netIO.calOnceIOCost(n)
        long multiCost = netIO.calMultiIOCost(n)

        then:
        println(onceCost)
        println(multiCost)

        where:
        n    | _
        10   | _
        100  | _
        1000 | _
    }

    @Unroll
    def "concurrentCost test"() {
        when:
        long singleThreadCost = Concurrent.calSingleThreadCost(n)
        long multiThreadCost = Concurrent.calMultiThreadCost(n)

        then:
        println(singleThreadCost)
        println(multiThreadCost)

        where:
        n     | _
        100   | _
        1000  | _
        10000 | _
    }

    @Unroll
    def "fileIOCost test"() {
        when:
        long onceReadCost = FileIO.calReadOnceCost()
        long multiReadCost = FileIO.calReadMultiCost()
        println(connection)

        then:
        println(onceReadCost)
        println(multiReadCost)
    }
}