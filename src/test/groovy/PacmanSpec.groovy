import groovy.util.logging.Log4j
import spock.genesis.Gen
import spock.lang.*

@Log4j
class PacmanSpec extends Specification {
	final static pacmanTokenFacingRight = ">"
	final static pacmanTokenFacingLeft = "<"

	@Unroll
	def "pacman eats the next dot on the right when it's placed after #beforeDotsCount dots and has #afterDotsCount after it and it's oriented towards right"() {
		given: "a line of dots with pacman in the middle oriented towards right"
		def initialBoard = lineOfDots(beforeDotsCount) + pacmanTokenFacingRight + lineOfDots(afterDotsCount)
		def expectedFinalBoard = lineOfDots(beforeDotsCount) + " " + pacmanTokenFacingRight + lineOfDots(afterDotsCount - 1)

		when: "tick"
		def boardAfterMove = tick(initialBoard, pacmanTokenFacingRight)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		beforeDotsCount << (0..<50)
		afterDotsCount << Gen.integer(1..100).take(50)
	}

	@Unroll
	def "pacman eats the next dot on the left when it's placed after #beforeDotsCount dots and has #afterDotsCount after it and it's oriented towards left"() {
		given: "a line of dots with pacman oriented towards left"
		def initialBoard = lineOfDots(beforeDotsCount) + pacmanTokenFacingLeft + lineOfDots(afterDotsCount)
		def expectedFinalBoard = lineOfDots(beforeDotsCount - 1) + pacmanTokenFacingLeft + " " + lineOfDots(afterDotsCount)

		when: "tick"
		def boardAfterMove = tick(initialBoard, pacmanTokenFacingLeft)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		beforeDotsCount << (1..50)
		afterDotsCount << Gen.integer(1..100).take(50)
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
