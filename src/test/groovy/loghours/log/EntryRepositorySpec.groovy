package loghours.log

import spock.lang.Specification

class EntryRepositorySpec extends Specification {

    UserRepository userRepo
    EntryRepository repo

    void setup() {
        userRepo = Mock(UserRepository)
        repo  = new EntryRepository.InMemory(userRepo)
    }

    def "should find "() {
        given:
        def email = 'abc@def.org'

        when:
        repo.save()

        then:
        e1.email == email
    }
}
