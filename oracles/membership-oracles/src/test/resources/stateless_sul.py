#!/usr/bin/env python3
import sys

def main():
    argv = len(sys.argv)

    for arg in sys.argv[1:]:
        print(sum([ord(c) for c in arg]))
        print(arg, file=sys.stderr)

    sys.exit(argv % 2)

if __name__ == "__main__":
    main()
