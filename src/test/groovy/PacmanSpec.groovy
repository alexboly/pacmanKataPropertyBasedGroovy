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

	enum Axis {
		None,
		Horizontal,
		Vertical
	}

	enum KindOfToken {
		Empty,
		Dot,
		PacmanLeft(directionOnAxis: DirectionOnAxis.Backward, axis: Axis.Horizontal, isMovable: true),
		PacmanRight(directionOnAxis: DirectionOnAxis.Forward, axis: Axis.Horizontal, isMovable: true),
		PacmanDown(directionOnAxis: DirectionOnAxis.Forward, axis: Axis.Vertical, isMovable: true),
		PacmanUp(directionOnAxis: DirectionOnAxis.Backward, axis: Axis.Vertical, isMovable: true)

		def directionOnAxis = DirectionOnAxis.None
		def axis = Axis.None
		def isMovable = false

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
		return Axis.values().findResult { nextAxis ->
			computeNextBoardOnAxis(board, Axis.Horizontal, nextAxis)
		}
	}

	private computeNextBoardOnAxis(board, currentAxis, nextAxis) {
		def movableTokensForAxis = KindOfToken.values().findAll { it.axis == nextAxis && it.isMovable }
		return rotateBoardOnAxis(
				computeNewBoard(
						rotateBoardOnAxis(board, currentAxis, nextAxis),
						movableTokensForAxis
				),
				nextAxis,
				currentAxis
		)
	}

	def rotateBoardOnAxis(board, startFromAxis, goToAxis) {
		def needsToRotate = (startFromAxis == Axis.Vertical && goToAxis == Axis.Horizontal) ||
		                    (startFromAxis == Axis.Horizontal && goToAxis == Axis.Vertical)

		return needsToRotate ? board.transpose() : board
	}

	def computeNewBoard(board, movableTokens) {
		def line = board.first()
		def intersection = line.intersect(movableTokens)
		def existingToken = intersection ? intersection.first() : null

		return existingToken ? [computeLineOrColumnAfterMove(line, existingToken)] : null
	}

	private computeLineOrColumnAfterMove(line, existingToken) {
		def beforeAndAfter = [before: beforeToken(line, existingToken), after: afterToken(line, existingToken)]
		def result = moveOnAxis(existingToken, beforeAndAfter)
		return result.before + [existingToken] + result.after
	}

	private moveOnAxis(existingToken, beforeAndAfter) {
		switch (existingToken.directionOnAxis) {
			case DirectionOnAxis.Forward:
				return computeNewBeforeAndNewAfterOnMoveForwardOnAxis(beforeAndAfter)

			case DirectionOnAxis.Backward:
				return computeNewBeforeAndNewAfterOnMoveBackwardOnAxis(beforeAndAfter)

			case DirectionOnAxis.None:
				return beforeAndAfter
		}
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

	def computeNewBeforeAndNewAfterOnMoveForwardOnAxis(beforeAndAfter) {
		def pacmanAttemptsToMoveBeyondTheEndOfTheLine = (beforeAndAfter.after.isEmpty())
		return pacmanAttemptsToMoveBeyondTheEndOfTheLine ?
		       [before: emptyPartialLine, after: emptySpaceAfter(minusFirst(beforeAndAfter.before))] :
		       [before: emptySpaceAfter(beforeAndAfter.before), after: minusFirst(beforeAndAfter.after)]
	}

	def computeNewBeforeAndNewAfterOnMoveBackwardOnAxis(beforeAndAfter) {
		return reverseBeforeAndAfterMap(computeNewBeforeAndNewAfterOnMoveForwardOnAxis(reverseBeforeAndAfterMap(beforeAndAfter)))
	}

	def reverseBeforeAndAfterMap(beforeAndAfter) {
		return [before: beforeAndAfter.after.reverse(), after: beforeAndAfter.before.reverse()]
	}

	def emptySpaceAfter(final partialLine) {
		partialLine + KindOfToken.Empty
	}

	def minusFirst(final def partialLine) {
		partialLine.takeRight(partialLine.size() - 1)
	}
}