import StatusCodes from 'http-status-codes';
import { Request, Response, Router } from 'express';

import { ParamMissingError } from '@shared/errors';
import { uploadRaw } from "@services/data-service";


// Constants
const router = Router();
const { CREATED, OK } = StatusCodes;

// Paths
export const p = {
    uploadFile: '/data/file',
    uploadRaw: '/data/raw'
} as const;

router.post(p.uploadRaw, async (req: Request, res: Response) => {
    uploadRaw(req.body);
    return res.status(OK);
});