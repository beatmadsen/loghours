package loghours.log

import spock.lang.Specification

import java.time.DayOfWeek
import java.time.LocalDateTime

class EntryRepositorySpec extends Specification {

    EntryRepository repo

    void setup() {
        repo = EntryRepository.inMemory()
    }

    def "should complete nested save and assign ids"() {
        given:
        def email = 'abc@def.org'
        def user = new User(email: email)
        def entry = new Entry(checkIn: LocalDateTime.now(), status: 'checked-in', user: user)

        when:
        def e1 = repo.save(entry)

        then:
        e1.id != 0L
        e1.user.id != 0L
    }

    def "should find saved item with correct day"() {
        given:
        def email = 'abc@def.org'
        def now = LocalDateTime.now()

        def saves = [
                [checkIn: now, email: email],
                [checkIn: now, email: 'flrub@cow.nl'],
                [checkIn: now.plusDays(1), email: email],
                [checkIn: now.plusDays(1), email: 'flrub@cow.nl'],
                [checkIn: now.minusDays(1), email: email],
                [checkIn: now.minusDays(1), email: 'flrub@cow.nl'],
        ].collect { data ->
            def entry = new Entry(
                    checkIn: data['checkIn'],
                    status: 'checked-in',
                    user: new User(email: data['email'])
            )
            repo.save(entry)
        }

        def correct = saves.first()

        when:
        def e1 = repo.findByDay(email, now).orElseThrow()

        then:
        e1.id != 0L
        e1.id == correct.id
        e1.user.email == email
    }

    def "should find saved items within correct week"() {
        given:
        def email = 'abc@def.org'
        def now = LocalDateTime.now().with(DayOfWeek.TUESDAY)

        def saves = [
                [checkIn: now, email: email],
                [checkIn: now, email: 'flrub@cow.nl'],
                [checkIn: now.plusDays(1), email: email],
                [checkIn: now.plusDays(1), email: 'flrub@cow.nl'],
                [checkIn: now.minusDays(1), email: email],
                [checkIn: now.minusDays(1), email: 'flrub@cow.nl'],
                [checkIn: now.minusDays(2), email: email],
                [checkIn: now.minusDays(2), email: 'flrub@cow.nl'],
                [checkIn: now.plusDays(6), email: email],
                [checkIn: now.plusDays(6), email: 'flrub@cow.nl'],
        ].collect { data ->
            def entry = new Entry(
                    checkIn: data['checkIn'],
                    status: 'checked-in',
                    user: new User(email: data['email'])
            )
            repo.save(entry)
        }

        def correct_items = saves.take(6).findAll { entry -> entry.user.email == email }

        when:
        def es = repo.findAllInWeek(email, now)

        then:
        es == correct_items
    }
}
