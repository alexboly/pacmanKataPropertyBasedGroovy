import groovy.util.logging.Log4j
import spock.genesis.Gen
import spock.lang.Specification

@Log4j
class PacmanSpec extends Specification {
	final static pacmanTokenFacingRight = ">"
	final static pacmanTokenFacingLeft = "<"
	final static emptySpace = " "
	final static dot = "."
	final static emptyPartialLine = []

	enum KindOfToken {
		Empty,
		Dot,
		PacmanLeft,
		PacmanRight

		@Override
		String toString() {
			if (this == Dot) return dot
			if (this == PacmanLeft) return pacmanTokenFacingLeft
			if (this == PacmanRight) return pacmanTokenFacingRight
			if (this == Empty) return emptySpace
			return ""
		}

		def plus(ArrayList collection) {
			[this] + collection
		}
	}

	def "pacman eats the next dot on the right when it has dots on the right and is oriented towards right"() {
		given: "a line of dots with pacman in the middle oriented towards right"
		def initialBoard = lineOfDots(beforeDotsCount) + KindOfToken.PacmanRight + lineOfDots(afterDotsCount)
		def expectedFinalBoard = lineOfDots(beforeDotsCount) + KindOfToken.Empty + KindOfToken.PacmanRight + lineOfDots(afterDotsCount - 1)

		when: "tick"
		def boardAfterMove = tick(initialBoard)

		then: "the final board is"
		boardAfterMove.join("") == expectedFinalBoard.join("")

		where: "dots count"
		beforeDotsCount << (0..<50)
		afterDotsCount << Gen.integer(1..100).take(50)
	}

	def "pacman eats the next dot on the left when it has dots on the left and it's oriented towards left"() {
		given: "a line of dots with pacman oriented towards left"
		def initialBoard = lineOfDots(beforeDotsCount) + KindOfToken.PacmanLeft + lineOfDots(afterDotsCount)
		def expectedFinalBoard = lineOfDots(beforeDotsCount - 1) + KindOfToken.PacmanLeft + KindOfToken.Empty + lineOfDots(afterDotsCount)

		when: "tick"
		def boardAfterMove = tick(initialBoard)

		then: "the final board is"
		boardAfterMove.join("") == expectedFinalBoard.join("")

		where: "dots count"
		beforeDotsCount << (1..50)
		afterDotsCount << Gen.integer(1..100).take(50)
	}

	def "pacman eats the last dot when all the way to the left and oriented towards left"() {
		given:
		def initialBoard = KindOfToken.PacmanLeft + lineOfDots(afterDotsCount)
		def expectedFinalBoard = KindOfToken.Empty + lineOfDots(afterDotsCount - 1) + KindOfToken.PacmanLeft

		when:
		def boardAfterMove = tick(initialBoard)

		then:
		boardAfterMove.join("") == expectedFinalBoard.join("")

		where:
		afterDotsCount << (1..100)
	}

	def "pacman eats the first dot when all the way to the right and oriented towards right"() {
		given:
		def initialBoard = lineOfDots(beforeDotsCount) + KindOfToken.PacmanRight
		def expectedFinalBoard = KindOfToken.PacmanRight + lineOfDots(beforeDotsCount - 1) + KindOfToken.Empty

		when:
		def boardAfterMove = tick(initialBoard)

		then:
		boardAfterMove.join("") == expectedFinalBoard.join("")

		where:
		beforeDotsCount << (1..100)
	}

	private static lineOfDots(final int dotsCount) {
		(1..<dotsCount + 1).collect { KindOfToken.Dot }
	}

	def tick(final board) {
		def possiblePacmanTokens = [KindOfToken.PacmanLeft, KindOfToken.PacmanRight]
		def existingToken = board.intersect(possiblePacmanTokens).first()
		def before = beforeToken(board, existingToken)
		def after = afterToken(board, existingToken)

		def result = computeNewBeforeAndNewAfter(before, after, existingToken)

		return result.before + existingToken + result.after
	}

	def beforeToken(line, token) {
		def tokenIndex = line.findIndexOf { it == token }
		def beforeSubLineTokenCount = tokenIndex
		return line.take(beforeSubLineTokenCount)
	}

	def afterToken(line, token) {
		def tokenIndex = line.findIndexOf { it == token }
		def afterSubLineTokenCount = line.size() - tokenIndex - 1
		return line.takeRight(afterSubLineTokenCount)
	}

	private computeNewBeforeAndNewAfter(before, after, pacmanToken) {
		if (pacmanToken == KindOfToken.PacmanLeft) {
			return computeNewBeforeAndNewAfterOnMoveLeft(before , after)
		}

		if (pacmanToken == KindOfToken.PacmanRight) {
			return computeNewBeforeAndNewAfterOnMoveRight(before, after)
		}
		return [[], []]
	}

	def computeNewBeforeAndNewAfterOnMoveRight(before, after) {
		def pacmanAttemptsToMoveBeyondTheEndOfTheLine = (after.isEmpty())
		return pacmanAttemptsToMoveBeyondTheEndOfTheLine ?
		       [before: emptyPartialLine, after: emptySpaceAfter(minusFirst(before))] :
		       [before: emptySpaceAfter(before), after: minusFirst(after)]
	}

	def computeNewBeforeAndNewAfterOnMoveLeft(before, after){
		def result = computeNewBeforeAndNewAfterOnMoveRight(after.reverse(), before.reverse())
		return [before: result.after.reverse(), after: result.before.reverse()]
	}

	def emptySpaceAfter(final partialLine) {
		partialLine + KindOfToken.Empty
	}

	def minusFirst(final def partialLine) {
		partialLine.takeRight(partialLine.size() - 1)
	}
}