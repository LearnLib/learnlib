#!/bin/python
import sys

def main():
    argv = len(sys.argv)
    limit = 3

    if argv > 1:
        for idx, arg in enumerate(sys.argv[1:]):
            if idx < limit:
                print(sum([ord(c) for c in arg]))
            else:
                print("error")

    if (argv < limit):
        sys.exit(argv % 2)
    else:
        sys.exit(1)

if __name__ == "__main__":
    main()
