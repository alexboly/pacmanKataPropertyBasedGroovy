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
		def lineWithPacman = lineOfDots(beforeDotsCount) + KindOfToken.PacmanRight + lineOfDots(afterDotsCount)
		def expectedLineWithPacman = lineOfDots(beforeDotsCount) + KindOfToken.Empty + KindOfToken.PacmanRight + lineOfDots(afterDotsCount - 1)
		def initialBoard = makeBoard(lineWithPacman)
		def expectedFinalBoard = makeBoard(expectedLineWithPacman)

		when: "tick"
		def boardAfterMove = tick(initialBoard)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		beforeDotsCount << (0..<50)
		afterDotsCount << Gen.integer(1..100).take(50)
		totalDotsCount = beforeDotsCount + afterDotsCount + 1
		beforeLineCount << (0..<50)
		afterLineCount << (0..<50)
		makeBoard = this.&makeBoardWithLineWithPacman.curry(beforeLineCount).rcurry(afterLineCount)
	}

	def "pacman eats the first dot when all the way to the right and oriented towards right"() {
		given:
		def lineWithPacman = lineOfDots(beforeDotsCount) + KindOfToken.PacmanRight
		def expectedLineWithPacman = KindOfToken.PacmanRight + lineOfDots(beforeDotsCount - 1) + KindOfToken.Empty

		def initialBoard = makeBoard(lineWithPacman)
		def expectedFinalBoard = makeBoard(expectedLineWithPacman)

		when:
		def boardAfterMove = tick(initialBoard)

		then:
		boardAfterMove == expectedFinalBoard

		where:
		beforeDotsCount << (1..100)
		totalDotsCount = beforeDotsCount + 1
		beforeLineCount << (0..<100)
		afterLineCount << (0..<100)
		makeBoard = this.&makeBoardWithLineWithPacman.curry(beforeLineCount).rcurry(afterLineCount)
	}

	def "pacman eats the next dot down when it has dots down and is oriented down"() {
		given: "a line of dots with pacman in the middle oriented towards right"
		def pacmanColumn = columnOfDots(beforeDotsCount) + [[KindOfToken.PacmanDown]] + columnOfDots(afterDotsCount)
		def initialBoard = makeBoard(pacmanColumn)
		def expectedPacmanColumn = columnOfDots(beforeDotsCount) + [[KindOfToken.Empty]] + [[KindOfToken.PacmanDown]] + columnOfDots(afterDotsCount - 1)
		def expectedFinalBoard = makeBoard(expectedPacmanColumn)

		when: "tick"
		def boardAfterMove = tick(initialBoard)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		beforeDotsCount << (0..<50)
		afterDotsCount << Gen.integer(1..100).take(50)
		totalDotsCount = beforeDotsCount + afterDotsCount + 1
		beforeColumnCount << (0..<50)
		afterColumnCount << (0..<50)
		makeBoard = this.&makeBoardWithColumnWithPacman.curry(beforeColumnCount).rcurry(afterColumnCount)
	}

	def "pacman eats the next dot up when it has dots up and is oriented up"() {
		given: "a line of dots with pacman in the middle oriented towards up"
		def pacmanColumn = columnOfDots(beforeDotsCount) + [[KindOfToken.PacmanUp]] + columnOfDots(afterDotsCount)
		def initialBoard = makeBoard(pacmanColumn)
		def expectedPacmanColumn = columnOfDots(beforeDotsCount - 1) + [[KindOfToken.PacmanUp]] + [[KindOfToken.Empty]] + columnOfDots(afterDotsCount)
		def expectedFinalBoard = makeBoard(expectedPacmanColumn)

		when: "tick"
		def boardAfterMove = tick(initialBoard)

		then: "the final board is"
		boardAfterMove == expectedFinalBoard

		where: "dots count"
		beforeDotsCount << (1..50)
		afterDotsCount << Gen.integer(1..100).take(50)
		beforeColumnCount << (0..<50)
		afterColumnCount << (0..<50)
		makeBoard = this.&makeBoardWithColumnWithPacman.curry(beforeColumnCount).rcurry(afterColumnCount)
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

	private static makeBoardWithLineWithPacman(final beforeLineCount, final lineWithPacman, final afterLineCount) {
		def totalDotsCount = lineWithPacman.size()
		return linesOfDots(beforeLineCount, totalDotsCount) +
		       [lineWithPacman] +
		       linesOfDots(afterLineCount, totalDotsCount)
	}

	private static makeBoardWithColumnWithPacman(final int beforeColumnCount, final pacmanColumn, final int afterColumnCount) {
		def totalDotsCount = pacmanColumn.size()
		return (
				linesOfDots(beforeColumnCount, totalDotsCount) +
				pacmanColumn.transpose() +
				linesOfDots(afterColumnCount, totalDotsCount)
		).transpose()
	}

	private static linesOfDots(final int linesCount, final int dotsCount) {
		(1..<linesCount + 1).collect { lineOfDots(dotsCount) }
	}

	private static lineOfDots(final int dotsCount) {
		(1..<dotsCount + 1).collect { KindOfToken.Dot }
	}

	static columnOfDots(final int dotsCount) {
		(1..<dotsCount + 1).collect { [KindOfToken.Dot] }
	}

	// Production code starts
	static def identityClosure = { it -> it }

	def tick(final board) {
		return Axis.values().findResult { nextAxis ->
			computeNextBoardOnAxis(board, Axis.Horizontal, nextAxis)
		}
	}

	private computeNextBoardOnAxis(final board, final currentAxis, final nextAxis) {
		def movableTokensForAxis = KindOfToken.values().findAll { it.axis == nextAxis && it.isMovable }
		def rotateForward = { aBoard -> rotateBoardOnAxis(aBoard, currentAxis, nextAxis) }
		def rotateBack = { aBoard -> rotateBoardOnAxis(aBoard, currentAxis, nextAxis) }
		return rotateBack(
				computeNewBoard(
						rotateForward(board),
						movableTokensForAxis
				)
		)
	}

	def rotateBoardOnAxis(final board, final startFromAxis, final goToAxis) {
		def axesAreDifferent = (startFromAxis != goToAxis)
		def noneOfTheAxesAreNeutral = (startFromAxis != Axis.None) && (goToAxis != Axis.None)
		def needsToRotate = axesAreDifferent && noneOfTheAxesAreNeutral
		return needsToRotate ? board.transpose() : board
	}

	def computeNewBoard(final board, final movableTokens) {
		board.findResult { line ->
			def token = findOneOfTheTokensInLine(line, movableTokens)
			if (token == null) return null
			else computeNewBoardWhenLineChanged(board, line, token)
		}
	}

	private static findOneOfTheTokensInLine(final line, final movableTokens) {
		return firstOrNull(line.intersect(movableTokens))
	}

	private computeNewBoardWhenLineChanged(final board, final line, final existingToken) {
		return computeNew(board,
		                  line,
		                  identityClosure,
		                  { theLine -> computeLineOrColumnAfterMove(theLine, existingToken) }
		)
	}

	private computeLineOrColumnAfterMove(final line, final existingToken) {
		return computeNew(line,
		                  existingToken,
		                  { beforeAndAfter -> moveOnAxis(existingToken, beforeAndAfter) },
		                  identityClosure
		)
	}

	private computeNew(final collection, final element, final Closure computeNewBeforeAndAfter, final Closure computeNewElement) {
		def beforeAndAfter = [before: beforeElement(collection, element), after: afterElement(collection, element)]
		def result = computeNewBeforeAndAfter(beforeAndAfter)
		return result.before + [computeNewElement(element)] + result.after
	}

	private moveOnAxis(final existingToken, final beforeAndAfter) {
		switch (existingToken.directionOnAxis) {
			case DirectionOnAxis.Forward:
				return computeNewBeforeAndNewAfterOnMoveForwardOnAxis(beforeAndAfter)

			case DirectionOnAxis.Backward:
				return computeNewBeforeAndNewAfterOnMoveBackwardOnAxis(beforeAndAfter)

			case DirectionOnAxis.None:
				return beforeAndAfter
		}
	}

	def beforeElement(final collection, final element) {
		def elementIndex = collection.findIndexOf { it == element }
		return collection.take(elementIndex)
	}

	def afterElement(final line, final element) {
		return beforeElement(line.reverse(), element).reverse()
	}

	def computeNewBeforeAndNewAfterOnMoveForwardOnAxis(final beforeAndAfter) {
		def pacmanAttemptsToMoveBeyondTheEndOfTheLine = (beforeAndAfter.after.isEmpty())
		return pacmanAttemptsToMoveBeyondTheEndOfTheLine ?
		       [before: emptyPartialLine, after: emptySpaceAfter(minusFirst(beforeAndAfter.before))] :
		       [before: emptySpaceAfter(beforeAndAfter.before), after: minusFirst(beforeAndAfter.after)]
	}

	def computeNewBeforeAndNewAfterOnMoveBackwardOnAxis(final beforeAndAfter) {
		return reverseBeforeAndAfterMap(computeNewBeforeAndNewAfterOnMoveForwardOnAxis(reverseBeforeAndAfterMap(beforeAndAfter)))
	}

	def reverseBeforeAndAfterMap(final beforeAndAfter) {
		return [before: beforeAndAfter.after.reverse(), after: beforeAndAfter.before.reverse()]
	}

	def emptySpaceAfter(final partialLine) {
		partialLine + KindOfToken.Empty
	}

	def minusFirst(final def partialLine) {
		partialLine.takeRight(partialLine.size() - 1)
	}

	static def firstOrNull(final def collection) {
		collection ? collection.first() : null
	}
}