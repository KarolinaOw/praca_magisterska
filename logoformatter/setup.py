#!/usr/bin/env python

from setuptools import setup

setup(
    name='logoformatter',
    version='0.1.0',
    install_requires=[
        'importlib-resources; python_version == "3.8"',
    ],
    package_data={'': ['template.eps']},
)