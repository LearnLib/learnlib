#!/bin/python
import sys

def main():
    argv = len(sys.argv)

    if argv > 1: # arg mode
        for arg in sys.argv[1:]:
            print(sum([ord(c) for c in arg]))
            print(arg, file=sys.stderr)
    else: # stdin mode
        for line in sys.stdin:
            for arg in line.split():
                argv += 1
                print(sum([ord(c) for c in arg]))
                print(arg, file=sys.stderr)

    sys.exit(argv % 2)

if __name__ == "__main__":
    main()
