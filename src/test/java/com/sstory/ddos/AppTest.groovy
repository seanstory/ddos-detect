package com.sstory.ddos

import spock.lang.Specification

class AppTest extends Specification {

    def "is it working?"(){
        when:
        App.main([] as String[])

        then:
        noExceptionThrown()
    }
}
