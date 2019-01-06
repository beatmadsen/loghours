package loghours.log

import spock.lang.Specification

class UserRepositorySpec extends Specification {

    def repo = UserRepository.inMemory()

    void setup() {
    }

    def "should create user when email is not found"() {
        given:
        def email = 'abc@def.org'

        when:
        def e1 = repo.findOrCreate(email)

        then:
        e1.email == email
    }

    def "should return existing user when email is found"() {
        given:
        def email = 'abc@def.org'
        def e1 = repo.findOrCreate(email)

        when:
        def e2 = repo.findOrCreate(email)

        then:
        e2.id == e1.id
    }

    def "should save a new user and assign a primary key to it"() {
        given:
        def email = 'clown@power.nu'
        def firstName = 'john'
        def user = new User(email: email, firstName: firstName, lastName: 'doe')

        when:
        repo.save(user)
        def u1 = repo.findOrCreate(email)

        then:
        u1.id != 0
        u1.email == email
        u1.firstName == firstName
    }

    def "should change an existing user and correctly index email"() {
        given:
        def oldEmail = 'clown@power.nu'
        def newEmail = 'za@pi.za'
        def user = repo.findOrCreate(oldEmail)
        def firstName = 'lot'

        when:
        user.email = newEmail
        user.firstName = firstName
        repo.save(user)
        def u1 = repo.findOrCreate(newEmail)
        def u2 = repo.findOrCreate(oldEmail)

        then:
        u1.id != u2.id
        u1.email == newEmail
        u2.email == oldEmail
        u1.firstName == firstName
        u2.firstName == null
    }
}
