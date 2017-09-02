import groovy.util.logging.Log4j
import spock.genesis.Gen
import spock.lang.*

@Log4j
class PacmanSpec extends Specification {
	final static pacmanTokenFacingRight = ">"
	final static pacmanTokenFacingLeft = "<"

	def "pacman eats the next dot on the right when it has dots on the right and is oriented towards right"() {
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

	def "pacman eats the next dot on the left when it has dots on the left and it's oriented towards left"() {
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

	def "pacman eats the last dot when all the way to the left and oriented towards left"() {
		given:
		def initialBoard = lineOfDots(0) + pacmanTokenFacingLeft + lineOfDots(afterDotsCount)
		def expectedFinalBoard = lineOfDots(0) + " " + lineOfDots(afterDotsCount - 1) + pacmanTokenFacingLeft

		when:
		def boardAfterMove = tick(initialBoard, pacmanTokenFacingLeft)

		then:
		boardAfterMove == expectedFinalBoard

		where:
		afterDotsCount << (1..100)
	}

	def "pacman eats the first dot when all the way to the right and oriented towards right"() {
		given:
		def initialBoard = lineOfDots(beforeDotsCount) + pacmanTokenFacingRight + lineOfDots(0)
		def expectedFinalBoard = pacmanTokenFacingRight + lineOfDots(beforeDotsCount - 1) + lineOfDots(0) + " "

		when:
		def boardAfterMove = tick(initialBoard, pacmanTokenFacingRight)

		then:
		boardAfterMove == expectedFinalBoard

		where:
		beforeDotsCount << (1..100)
	}

	private static lineOfDots(int dotsCount) {
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
				return { initialBoard, token ->
					def indexOfToken = initialBoard.indexOf(token)
					(indexOfToken == 0) ? initialBoard.size() - 1 : indexOfToken - 1
				}

			case pacmanTokenFacingRight:
				return { initialBoard, token ->
					def indexOfToken = initialBoard.indexOf(token)
					(indexOfToken == initialBoard.size() - 1) ? 0 : indexOfToken + 1
				}

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
