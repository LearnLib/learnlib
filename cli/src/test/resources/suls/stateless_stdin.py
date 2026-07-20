#!/bin/python
import sys

def main():
    argv = len(sys.argv)

    for line in sys.stdin:
        for arg in line.split():
            argv += 1
            print(sum([ord(c) for c in arg]))

    sys.exit(argv % 2)

if __name__ == "__main__":
    main()
