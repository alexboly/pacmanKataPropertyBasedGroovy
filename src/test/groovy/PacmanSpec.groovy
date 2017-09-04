import groovy.util.logging.Log4j
import spock.genesis.Gen
import spock.lang.Specification

@Log4j
class PacmanSpec extends Specification {
	final static pacmanTokenFacingRight = ">"
	final static pacmanTokenFacingLeft = "<"
	final static pacmanTokenFacingDown = "v"
	final static pacmanTokenFacingUp = "^"
	final static emptySpace = " "
	final static dot = "."
	final static emptyPartialLine = []

	enum DirectionOnAxis {
		None,
		Forward,
		Backward
	}

	enum KindOfToken {
		Empty(directionOnAxis: DirectionOnAxis.None),
		Dot(directionOnAxis: DirectionOnAxis.None),
		PacmanLeft(directionOnAxis: DirectionOnAxis.Backward),
		PacmanRight(directionOnAxis: DirectionOnAxis.Forward),
		PacmanDown(directionOnAxis: DirectionOnAxis.Forward),
		PacmanUp(directionOnAxis: DirectionOnAxis.Backward)

		def directionOnAxis

		@Override
		String toString() {
			if (this == Dot) return dot
			if (this == PacmanLeft) return pacmanTokenFacingLeft
			if (this == PacmanRight) return pacmanTokenFacingRight
			if (this == PacmanDown) return pacmanTokenFacingDown
			if (this == PacmanUp) return pacmanTokenFacingUp
			if (this == Empty) return emptySpace
			return ""
		}

		def plus(ArrayList collection) {
			[this] + collection
		}

		def transpose() {
			if (this == Empty) return Empty
			if (this == Dot) return Dot
			if (this == PacmanLeft) return PacmanDown
			if (this == PacmanRight) return PacmanUp
			if (this == PacmanDown) return PacmanLeft
			if (this == PacmanUp) return PacmanLeft
		}
	}

	def "pacman eats the next dot on the right when it has dots on the right and is oriented towards right"() {
		given: "a line of dots with pacman in the middle oriented towards right"
		def initialBoard = [lineOfDots(beforeDotsCount) + KindOfToken.PacmanRight + lineOfDots(afterDotsCount)]
		def expectedFinalBoard = [lineOfDots(beforeDotsCount) + KindOfToken.Empty + KindOfToken.PacmanRight + lineOfDots(afterDotsCount - 1)]

		when: "tick"
		def boardAfterMove = tick(initialBoard)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		beforeDotsCount << (0..<50)
		afterDotsCount << Gen.integer(1..100).take(50)
	}

	def "pacman eats the first dot when all the way to the right and oriented towards right"() {
		given:
		def initialBoard = [lineOfDots(beforeDotsCount) + KindOfToken.PacmanRight]
		def expectedFinalBoard = [KindOfToken.PacmanRight + lineOfDots(beforeDotsCount - 1) + KindOfToken.Empty]

		when:
		def boardAfterMove = tick(initialBoard)

		then:
		boardAfterMove == expectedFinalBoard

		where:
		beforeDotsCount << (1..100)
	}

	def "pacman eats the next dot down when it has dots down and is oriented down"() {
		given: "a line of dots with pacman in the middle oriented towards right"
		def initialBoard = columnOfDots(beforeDotsCount) + [[KindOfToken.PacmanDown]] + columnOfDots(afterDotsCount)
		def expectedFinalBoard = columnOfDots(beforeDotsCount) + [[KindOfToken.Empty]] + [[KindOfToken.PacmanDown]] + columnOfDots(afterDotsCount - 1)

		when: "tick"
		def boardAfterMove = tick(initialBoard)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		beforeDotsCount << (0..<50)
		afterDotsCount << Gen.integer(1..100).take(50)
	}

	def "pacman eats the next dot up when it has dots up and is oriented up"() {
		given: "a line of dots with pacman in the middle oriented towards up"
		def initialBoard = columnOfDots(beforeDotsCount) + [[KindOfToken.PacmanUp]] + columnOfDots(afterDotsCount)
		def expectedFinalBoard = columnOfDots(beforeDotsCount - 1) + [[KindOfToken.PacmanUp]] + [[KindOfToken.Empty]] + columnOfDots(afterDotsCount)

		when: "tick"
		def boardAfterMove = tick(initialBoard)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		beforeDotsCount << (1..50)
		afterDotsCount << Gen.integer(1..100).take(50)
	}

	def "pacman eats the next dot on the left when it has dots on the left and it's oriented towards left"() {
		given: "a line of dots with pacman oriented towards left"
		def initialBoard = [lineOfDots(beforeDotsCount) + KindOfToken.PacmanLeft + lineOfDots(afterDotsCount)]
		def expectedFinalBoard = [lineOfDots(beforeDotsCount - 1) + KindOfToken.PacmanLeft + KindOfToken.Empty + lineOfDots(afterDotsCount)]

		when: "tick"
		def boardAfterMove = tick(initialBoard)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		beforeDotsCount << (1..50)
		afterDotsCount << Gen.integer(1..100).take(50)
	}

	def "pacman eats the last dot when all the way to the left and oriented towards left"() {
		given:
		def initialBoard = [KindOfToken.PacmanLeft + lineOfDots(afterDotsCount)]
		def expectedFinalBoard = [KindOfToken.Empty + lineOfDots(afterDotsCount - 1) + KindOfToken.PacmanLeft]

		when:
		def boardAfterMove = tick(initialBoard)

		then:
		boardAfterMove == expectedFinalBoard

		where:
		afterDotsCount << (1..100)
	}

	private static lineOfDots(final int dotsCount) {
		(1..<dotsCount + 1).collect { KindOfToken.Dot }
	}

	static columnOfDots(final int dotsCount) {
		(1..<dotsCount + 1).collect { [KindOfToken.Dot] }
	}

	def tick(final board) {
		return computeNewBoard(board, [KindOfToken.PacmanLeft, KindOfToken.PacmanRight])
	}

	def computeNewBoard(board, possiblePacmanTokens) {
		def line = board.first()
		def intersection = line.intersect(possiblePacmanTokens)
		def existingToken = intersection ? intersection.first() : null

		if (existingToken) {
			return [computeLineOrColumnAfterMove(line, existingToken)]
		} else {
			return computeNewBoard(board.transpose(), possiblePacmanTokens.collect { it.transpose() }).transpose()
		}
	}

	private computeLineOrColumnAfterMove(line, existingToken) {
		def result
		def before = beforeToken(line, existingToken)
		def after = afterToken(line, existingToken)
		if (existingToken.directionOnAxis == DirectionOnAxis.Backward) result = computeNewBeforeAndNewAfterOnMoveBackwardOnAxis(before, after)
		if (existingToken.directionOnAxis == DirectionOnAxis.Forward) result = computeNewBeforeAndNewAfterOnMoveForwardOnAxis(before, after)
		return result.before + [existingToken] + result.after
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

	def computeNewBeforeAndNewAfterOnMoveForwardOnAxis(before, after) {
		def pacmanAttemptsToMoveBeyondTheEndOfTheLine = (after.isEmpty())
		return pacmanAttemptsToMoveBeyondTheEndOfTheLine ?
		       [before: emptyPartialLine, after: emptySpaceAfter(minusFirst(before))] :
		       [before: emptySpaceAfter(before), after: minusFirst(after)]
	}

	def computeNewBeforeAndNewAfterOnMoveBackwardOnAxis(before, after) {
		def result = computeNewBeforeAndNewAfterOnMoveForwardOnAxis(after.reverse(), before.reverse())
		return [before: result.after.reverse(), after: result.before.reverse()]
	}

	def emptySpaceAfter(final partialLine) {
		partialLine + KindOfToken.Empty
	}

	def minusFirst(final def partialLine) {
		partialLine.takeRight(partialLine.size() - 1)
	}
}