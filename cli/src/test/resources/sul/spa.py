#!/usr/bin/env python3
import sys

def main():
	argv = len(sys.argv)

	if argv > 1:
		if sys.argv[1] == "S":
			print("ok", end="")
			if argv > 2:
				arg = sys.argv[2]
				if arg == "a" or arg == "b":
					print(" ", end="")
					print(sum([ord(c) for c in arg]), end="")
					if argv > 3:
						if sys.argv[3] == "R":
							print(" ok", end="")
							if argv > 4:
								print(" error" * (argv - 4), end="")
								print()
								sys.exit(1)
							else:
								print()
								sys.exit(0)
						else:
							print(" error" * (argv - 3), end="")
				else:
					print(" error" * (argv - 2), end="")
		else:
			print(" error" * argv, end="")

	print()
	sys.exit(1)

if __name__ == "__main__":
    main()
