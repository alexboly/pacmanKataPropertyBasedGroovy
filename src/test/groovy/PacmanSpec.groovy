import groovy.util.logging.Log4j
import spock.lang.*

@Log4j
class PacmanSpec extends Specification {
	private String pacmanToken = '>'

	@Unroll
	def "given a line of #dotsCount dots with pacman on the left oriented towards right, pacman eats the next dot on the right"() {
		given: "a line of dots with pacman on the left oriented towards right"
		def initialBoard = pacmanToken + lineOfDots(dotsCount)
		def expectedFinalBoard = ' ' + pacmanToken + lineOfDots(dotsCount - 1)

		when: "tick"
		def boardAfterMove = tick(initialBoard)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		dotsCount << (2..100)
	}

	private lineOfDots(int dotsCount) {
		return (1..dotsCount).collect { '.' }.join('')
	}

	def tick(String board) {
		def i = board.indexOf(pacmanToken)
		def a = board.toCharArray()
		a[i + 1] = pacmanToken
		a[i] = ' '
		return a.toString()
	}
}
