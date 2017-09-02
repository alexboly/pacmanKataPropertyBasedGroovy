import groovy.util.logging.Log4j
import spock.genesis.Gen
import spock.lang.Specification

@Log4j
class PacmanSpec extends Specification {
	final static pacmanTokenFacingRight = ">"
	final static pacmanTokenFacingLeft = "<"
	final static emptySpace = " "

	def "pacman eats the next dot on the right when it has dots on the right and is oriented towards right"() {
		given: "a line of dots with pacman in the middle oriented towards right"
		def initialBoard = [lineOfDots(beforeDotsCount), pacmanTokenFacingRight, lineOfDots(afterDotsCount)].join("")
		def expectedFinalBoard = [lineOfDots(beforeDotsCount), emptySpace, pacmanTokenFacingRight, lineOfDots(afterDotsCount - 1)].join("")

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
		def initialBoard = [lineOfDots(beforeDotsCount), pacmanTokenFacingLeft, lineOfDots(afterDotsCount)].join("")
		def expectedFinalBoard = [lineOfDots(beforeDotsCount - 1), pacmanTokenFacingLeft, emptySpace, lineOfDots(afterDotsCount)].join("")

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
		def initialBoard = [pacmanTokenFacingLeft, lineOfDots(afterDotsCount)].join("")
		def expectedFinalBoard = [emptySpace, lineOfDots(afterDotsCount - 1), pacmanTokenFacingLeft].join("")

		when:
		def boardAfterMove = tick(initialBoard, pacmanTokenFacingLeft)

		then:
		boardAfterMove == expectedFinalBoard

		where:
		afterDotsCount << (1..100)
	}

	def "pacman eats the first dot when all the way to the right and oriented towards right"() {
		given:
		def initialBoard = [lineOfDots(beforeDotsCount), pacmanTokenFacingRight].join("")
		def expectedFinalBoard = [pacmanTokenFacingRight, lineOfDots(beforeDotsCount - 1), emptySpace].join("")

		when:
		def boardAfterMove = tick(initialBoard, pacmanTokenFacingRight)

		then:
		boardAfterMove == expectedFinalBoard

		where:
		beforeDotsCount << (1..100)
	}

	private static lineOfDots(final int dotsCount) {
		return (1..<dotsCount + 1).collect { "." }.join("")
	}


	def tick(final board, final pacmanToken) {
		def beforeAndAfter = board.split(pacmanToken)
		def before = beforeAndAfter[0]
		def after = beforeAndAfter.size() == 2 ? beforeAndAfter[1] : ""

		def emptyPartialLine = ""
		def newBefore = ""
		def newAfter = ""

		if (pacmanToken == pacmanTokenFacingRight && after.isEmpty()) {
			newBefore = emptyPartialLine
			newAfter = emptySpaceAfter(minusFirst(before))
		}
		if (pacmanToken == pacmanTokenFacingRight && !after.isEmpty()) {
			newBefore = emptySpaceAfter(before)
			newAfter = minusFirst(after)
		}

		if (pacmanToken == pacmanTokenFacingLeft && before.isEmpty()) {
			newBefore = emptySpaceAfter(before) + minusLast(after)
			newAfter = emptyPartialLine
		}

		if (pacmanToken == pacmanTokenFacingLeft && !before.isEmpty()) {
			newBefore = minusLast(before)
			newAfter = emptySpaceBefore(after)
		}

		return [newBefore, pacmanToken, newAfter].join("")
	}

	def emptySpaceAfter(final partialLine) {
		partialLine + emptySpace
	}

	def emptySpaceBefore(final partialLine) {
		emptySpace + partialLine
	}

	def minusLast(final def partialLine) {
		partialLine.substring(0, partialLine.size() - 1)
	}

	def minusFirst(final def partialLine) {
		partialLine.substring(1)
	}
}
