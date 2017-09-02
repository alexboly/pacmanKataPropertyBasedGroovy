import groovy.util.logging.Log4j
import spock.lang.*

@Log4j
class PacmanSpec extends Specification {
	private String pacmanTokenFacingRight = '>'

	@Unroll
	def "given a line of #dotsCount dots with pacman on the left oriented towards right, pacman eats the next dot on the right"() {
		given: "a line of dots with pacman on the left oriented towards right"
		def initialBoard = pacmanTokenFacingRight + lineOfDots(dotsCount)
		def expectedFinalBoard = ' ' + pacmanTokenFacingRight + lineOfDots(dotsCount - 1)

		when: "tick"
		def boardAfterMove = tick(initialBoard, pacmanTokenFacingRight)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		dotsCount << (1..100)
	}

	private lineOfDots(int dotsCount) {
		return (1..<dotsCount + 1).collect { '.' }.join('')
	}

	def tick(final board, final pacmanToken) {
		return boardAfterPacmanMovedToNexPosition(
				boardAfterPacmanMovedFromCurrentPosition(board, pacmanToken),
				positionAfterPacmanMovesRight(board, pacmanToken),
				pacmanToken
		).join("")
	}

	private positionAfterPacmanMovesRight(final board, final pacmanToken) {
		return board.indexOf(pacmanToken) + 1
	}

	def boardAfterPacmanMovedFromCurrentPosition(final board, final pacmanToken) {
		return board.collect { it == pacmanToken ? " " : it }
	}

	def boardAfterPacmanMovedToNexPosition(final board, final pacmanNextPosition, final pacmanToken) {
		return board.indexed().collect { index, item -> (index == pacmanNextPosition) ? pacmanToken : item }
	}
}
