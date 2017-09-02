import spock.lang.Specification

class PacmanSpec extends Specification {
	def "first spec"() {
		given: "an initial board"
		def board = initialBoard

		when: "tick"
		def boardAfterMove = tick(board)

		then: "the final board is"
		boardAfterMove == finalBoard

		where: "initial board, set of moves and final board"
		initialBoard || finalBoard
		">."         || " >"
		">.."        || " >."
		">..."        || " >.."
	}

	def tick(String board) {
		def i = board.indexOf('>')
		def a = board.toCharArray()
		a[i + 1] = '>'
		a[i] = ' '
		return a.toString()
	}
}
