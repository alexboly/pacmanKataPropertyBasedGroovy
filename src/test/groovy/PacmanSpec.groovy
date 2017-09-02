import groovy.util.logging.Log4j
import spock.genesis.Gen
import spock.lang.*

@Log4j
class PacmanSpec extends Specification {
	final static pacmanTokenFacingRight = ">"
	final static pacmanTokenFacingLeft = "<"

	def "given a line of #dotsCount dots with pacman on the left oriented towards right, pacman eats the next dot on the right"() {
		given: "a line of dots with pacman on the left oriented towards right"
		def initialBoard = pacmanTokenFacingRight + lineOfDots(dotsCount)
		def expectedFinalBoard = " " + pacmanTokenFacingRight + lineOfDots(dotsCount - 1)

		when: "tick"
		def boardAfterMove = tick(initialBoard, pacmanTokenFacingRight)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		dotsCount << (1..100)
	}


	def "given a line of #beforeDotsCount dots before pacman and #afterDotsCount after pacman oriented towards right, pacman eats the next dot on the right"() {
		given: "a line of dots with pacman on the left oriented towards right"
		def initialBoard = lineOfDots(beforeDotsCount) + pacmanTokenFacingRight + lineOfDots(afterDotsCount)
		def expectedFinalBoard = lineOfDots(beforeDotsCount) + " " + pacmanTokenFacingRight + lineOfDots(afterDotsCount - 1)

		when: "tick"
		def boardAfterMove = tick(initialBoard, pacmanTokenFacingRight)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		beforeDotsCount << (1..50)
		afterDotsCount << Gen.integer(1..50).take(50)
	}

	def "given a line of #dotsCount dots with pacman on the right oriented towards left, pacman eats the next dot on the left"() {
		given: "a line of dots with pacman on the right oriented towards left"
		def initialBoard = lineOfDots(dotsCount) + pacmanTokenFacingLeft
		def expectedFinalBoard = lineOfDots(dotsCount - 1) + pacmanTokenFacingLeft + " "

		when: "tick"
		def boardAfterMove = tick(initialBoard, pacmanTokenFacingLeft)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		dotsCount << (1..100)
	}

	private lineOfDots(int dotsCount) {
		return (1..<dotsCount + 1).collect { "." }.join("")
	}

	def tick(final board, final pacmanToken) {
		def computeNextPositionFunction = nextPositionFunctionByOrientation(pacmanToken)

		return boardAfterPacmanMovedToNexPosition(
				boardAfterPacmanMovedFromCurrentPosition(board, pacmanToken),
				computeNextPositionFunction(board, pacmanToken),
				pacmanToken
		).join("")
	}

	private static nextPositionFunctionByOrientation(pacmanToken) {
		switch (pacmanToken) {
			case pacmanTokenFacingLeft:
				return { initialBoard, token -> initialBoard.indexOf(token) - 1 }

			case pacmanTokenFacingRight:
				return { initialBoard, token -> initialBoard.indexOf(token) + 1 }

			default:
				return { _, __ -> "" }
		}
	}

	static boardAfterPacmanMovedFromCurrentPosition(final board, final pacmanToken) {
		return board.collect { it == pacmanToken ? " " : it }
	}

	static boardAfterPacmanMovedToNexPosition(final board, final pacmanNextPosition, final pacmanToken) {
		return board.indexed().collect { index, item -> (index == pacmanNextPosition) ? pacmanToken : item }
	}
}
