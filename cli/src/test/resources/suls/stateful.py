#!/bin/python
from pathlib import Path

import sys
import pickle
import os.path

def main():
    folder = Path(sys.argv[0]).parent
    path = os.path.join(folder, "state.p")
    argv = 1

    if os.path.isfile(path):
        argv = pickle.load(open(path, "rb"))

    if len(sys.argv) > 1:
        for arg in sys.argv[1:]:
            if arg == "reset":
                argv = 1
            else:
                argv += 1
                print(sum([ord(c) for c in arg]))

    pickle.dump(argv, open(path, "wb"))
    sys.exit(argv % 2)

if __name__ == "__main__":
    main()
