import groovy.util.logging.Log4j
import spock.lang.*

@Log4j
class PacmanSpec extends Specification {
	def pacmanTokenFacingRight = '>'
	def pacmanTokenFacingLeft = '<'

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

	@Unroll
	def "given a line of #dotsCount dots with pacman on the right oriented towards left, pacman eats the next dot on the left"() {
		given: "a line of dots with pacman on the right oriented towards left"
		def initialBoard = lineOfDots(dotsCount) + pacmanTokenFacingLeft
		def expectedFinalBoard = lineOfDots(dotsCount - 1) + pacmanTokenFacingLeft + ' '

		when: "tick"
		def boardAfterMove = tick(initialBoard, pacmanTokenFacingLeft)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		dotsCount << (1..100)
	}

	private lineOfDots(int dotsCount) {
		return (1..<dotsCount + 1).collect { '.' }.join('')
	}

	def tick(final board, final pacmanToken) {
		if (pacmanToken == pacmanTokenFacingRight) {
			return boardAfterPacmanMovedToNexPosition(
					boardAfterPacmanMovedFromCurrentPosition(board, pacmanToken),
					positionAfterPacmanMovesRight(board, pacmanToken),
					pacmanToken
			).join("")
		}

		return boardAfterPacmanMovedToNexPosition(
				boardAfterPacmanMovedFromCurrentPosition(board, pacmanToken),
				positionAfterPacmanMovesLeft(board, pacmanToken),
				pacmanToken
		).join("")
	}

	static positionAfterPacmanMovesRight(final board, final pacmanToken) {
		return board.indexOf(pacmanToken) + 1
	}

	static positionAfterPacmanMovesLeft(final board, final pacmanToken) {
		return board.indexOf(pacmanToken) - 1
	}

	static boardAfterPacmanMovedFromCurrentPosition(final board, final pacmanToken) {
		return board.collect { it == pacmanToken ? " " : it }
	}

	static boardAfterPacmanMovedToNexPosition(final board, final pacmanNextPosition, final pacmanToken) {
		return board.indexed().collect { index, item -> (index == pacmanNextPosition) ? pacmanToken : item }
	}
}
