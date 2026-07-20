#!/bin/python
import sys

def main():
    argv = len(sys.argv)

    if argv > 1:
        for arg in sys.argv[1:]:
            print(sum([ord(c) for c in arg]))

    sys.exit(argv % 2)

if __name__ == "__main__":
    main()
